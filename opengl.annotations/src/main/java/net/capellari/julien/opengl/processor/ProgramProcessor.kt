package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import net.capellari.julien.opengl.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic

@RequiresApi(26)
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
internal class ProgramProcessor : AbstractProcessor() {
    // Propriétés
    private val sourceRoot get() = processingEnv.options["kapt.kotlin.generated"]!!

    // Méthodes
    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
                Program::class.java.canonicalName
        )
    }
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        // Générate implementation code
        roundEnv.getElementsAnnotatedWith(Program::class.java)
                .forEach {
                    if (it is TypeElement && programCheck(it)) {
                        createProgramImpl(it)
                    }
                }

        return false
    }

    // - tests
    private fun programCheck(type: TypeElement) : Boolean {
        // Get annotation
        val program = type.getAnnotation(Program::class.java)
        var hasFragment = false
        var hasVertex = false

        // should inherit from BaseProgram
        if (!Utils.inherit(processingEnv, type, "net.capellari.julien.opengl.BaseProgram")) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${type.simpleName} should inherit from BaseProgram")
            return false
        }

        // Check Scripts
        program.shaders.scripts.forEach {
            if ((it.file != "") or (it.script != "")) {
                when (it.type) {
                    ShaderType.FRAGMENT -> hasFragment = true
                    ShaderType.VERTEX   -> hasVertex = true
                    ShaderType.GEOMETRY -> {}
                    ShaderType.COMPUTE  -> {}
                }
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "One ${type.simpleName}'s ShaderScript must have a file or a script")
            }
        }

        // Check consistency
        if (!hasFragment) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${type.simpleName} has no fragment shader")
        }

        if (!hasVertex) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${type.simpleName} has no vertex shader")
        }

        return true
    }
    private fun createProgramImpl(element: TypeElement) {
        // Get infos
        val pkgProgram = processingEnv.elementUtils.getPackageOf(element).toString()
        val program = element.getAnnotation(Program::class.java)

        // Classes
        val baseCls = ClassName(pkgProgram, element.simpleName.toString())
        val implCls = ClassName(pkgProgram, "${element.simpleName}_Impl")

        // Attributes
        val attrs = mutableListOf<AttributeProperty>()
        val unifs = mutableListOf<UniformProperty>()
        val unifBlocks = mutableMapOf<String, UniformBlockProperty>()
        val ssbos = mutableMapOf<String, ShaderStorageProperty>()

        var meshMaterialAttribute: UniformProperty? = null

        // Attributs
        program.attributs.forEach {
            attrs.add(AttributeProperty(it))
        }

        // Liste des attributs
        val fields = mutableMapOf<String,VariableElement>()
        element.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD) fields[it.simpleName.toString()] = it as VariableElement
        }

        // Analyse des annotations attributs
        element.enclosedElements
                .forEach {
                    val name = it.simpleName.split("$").first()

                    // Annotations
                    val unif = it.getAnnotation(Uniform::class.java)
                    val unifBlock = it.getAnnotation(UniformBlock::class.java)
                    val ssbo = it.getAnnotation(ShaderStorage::class.java)

                    // Attributs & Uniforms
                    if (unif != null) {
                        val prop = UniformProperty(fields[name]!!, unif)

                        if (unif.meshMaterial) {
                            meshMaterialAttribute = prop
                        } else {
                            unifs.add(prop)
                        }
                    } else if (unifBlock != null) {
                        if (!unifBlocks.containsKey(unifBlock.name)) {
                            unifBlocks[unifBlock.name] = UniformBlockProperty(unifBlock)
                        }

                        unifBlocks[unifBlock.name]!!.add(fields[name]!!)
                    } else if (ssbo != null) {
                        if (!ssbos.containsKey(ssbo.name)) {
                            ssbos[ssbo.name] = ShaderStorageProperty(ssbo)
                        }

                        ssbos[ssbo.name]!!.add(fields[name]!!)
                    }
                }

        // Méthodes
        val loadShaders = FunSpec.builder("loadShaders")
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("context", ClassName("android.content", "Context"))
                    addParameter("program", Int::class)

                    for (shader in program.shaders.scripts) {
                        if (shader.file != "") {
                            addStatement(
                                    "GLES31.glAttachShader(program, loadShaderAsset(context, %S, %T.%L))",
                                    shader.file,
                                    ShaderType::class, shader.type.name
                            )
                        } else if (shader.script != "") {
                            addStatement(
                                    "GLES31.glAttachShader(program, loadShader(%S, %T.%L))",
                                    shader.script,
                                    ShaderType::class, shader.type.name
                            )
                        }
                    }
                }.build()

        val getLocations = FunSpec.builder("getLocations")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in attrs) prop.getLocationFunc(this)

                    if (!unifBlocks.isEmpty()) {
                        addStatement("var binding = 0")
                        for (prop in unifBlocks.values) {
                            beginControlFlow("if (bindUniformBlock(%S, binding))", prop.annotation.name)
                                addStatement("%N = binding++", prop.binding)
                            endControlFlow()
                        }
                    }

                    for (prop in ssbos.values) prop.bindFunc(this)
                }.build()

        val genBuffers = FunSpec.builder("genBuffers")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in unifBlocks.values) prop.genBufferFunc(this)
                    for (prop in ssbos.values)      prop.genBufferFunc(this)
                }.build()

        val loadUniforms = FunSpec.builder("loadUniforms")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in unifs) {
                        addStatement("setUniformValue(%S, %N)", prop.annotation.name, prop.property)
                    }
                }.build()

        val loadBuffers = FunSpec.builder("loadBuffers")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in unifBlocks.values) prop.loadBufferFunc(this)
                    for (prop in ssbos.values)      prop.loadBufferFunc(this)
                }.build()

        val enableVBO = FunSpec.builder("enableVBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("mesh", ClassName("net.capellari.julien.opengl.base", "BaseMesh"))

                    // Link to arrays
                    for (prop in attrs) {
                        when (prop.annotation.type) {
                            AttributeType.VERTICES -> {
                                addStatement("GLES31.glEnableVertexAttribArray(%N)", prop.handle)
                                addStatement("GLES31.glVertexAttribPointer(%N, 3, mesh.vertexType, %L, 0, 0)", prop.handle, prop.annotation.normalized)
                            }
                            AttributeType.NORMALS -> {
                                beginControlFlow("if (mesh.hasNormals)")
                                    addStatement("GLES31.glEnableVertexAttribArray(%N)", prop.handle)
                                    addStatement("GLES31.glVertexAttribPointer(%N, 3, mesh.normalType, %L, 0, mesh.vertexSize)", prop.handle, prop.annotation.normalized)
                                endControlFlow()
                            }
                        }
                    }
                }.build()

        val setMeshMaterial = FunSpec.builder("setMeshMaterial")
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("mesh", ClassName("net.capellari.julien.opengl.base", "BaseMesh"))

                    meshMaterialAttribute?.also {
                        // Chargement du material
                        addStatement("setUniformValue(%S, mesh.getMaterial());", it.annotation.name)
                    }
                }.build()

        // Implementation de la classe
        val cls = TypeSpec.classBuilder(implCls)
            .apply {
                superclass(baseCls)

                // Attributs
                addProperties(attrs)
                addProperties(unifs)
                addProperties(unifBlocks.values)
                addProperties(ssbos.values)

                // Contructeur
                if (program.mode != 0) {
                    addInitializerBlock(CodeBlock.builder()
                            .apply {
                                addStatement("mode = %L", program.mode)
                                addStatement("defaultMode = %L", program.mode)
                            }.build()
                    )
                }

                // Méthodes
                addFunction(loadShaders)
                addFunction(getLocations)
                addFunction(genBuffers)

                addFunction(loadUniforms)
                addFunction(loadBuffers)
                addFunction(enableVBO)
                addFunction(setMeshMaterial)
            }.build()

        // Ecriture
        val code = FileSpec.builder(pkgProgram, implCls.simpleName)
                .apply {
                    addImport("android.opengl", "GLES31")
                    addImport("android.util", "Log")
                    addImport("net.capellari.julien.opengl", "GLUtils")

                    addType(cls)
                }.build()

        // Create file
        Utils.writeTo(sourceRoot, code)
    }
}