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
                Program::class.java.canonicalName,
                UniformBlock::class.java.canonicalName,
                SharedStorage::class.java.canonicalName
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

        roundEnv.getElementsAnnotatedWith(UniformBlock::class.java)
                .forEach {
                    if (it is TypeElement  && uniformBlockCheck(it)) {
                        createUniformBlockImpl(it)
                    }
                }

        roundEnv.getElementsAnnotatedWith(SharedStorage::class.java)
                .forEach {
                    if (it is TypeElement  && sharedStorageBlockCheck(it)) {
                        createSharedStorageBlockImpl(it)
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
        if (!Utils.inherit(processingEnv, type, "net.capellari.julien.opengl.base.BaseProgram")) {
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
    private fun uniformBlockCheck(type: TypeElement) : Boolean {
        // should inherit from BaseUniformBlock
        if (!Utils.inherit(processingEnv, type, "net.capellari.julien.opengl.base.BaseUniformBlock")) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${type.simpleName} should inherit from BaseUniformBlock")
            return false
        }

        return true
    }
    private fun sharedStorageBlockCheck(type: TypeElement) : Boolean {
        // should inherit from BaseUniformBlock
        if (!Utils.inherit(processingEnv, type, "net.capellari.julien.opengl.base.BaseSharedStorageBlock")) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${type.simpleName} should inherit from BaseSharedStorageBlock")
            return false
        }

        return true
    }

    // - implementations
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
        val unifBlocks = mutableListOf<UniformBlockProperty>()
        val ssbos = mutableListOf<ShaderStorageProperty>()

        var meshMaterialAttribute: UniformProperty? = null

        // Attributs
        var others = ""
        program.attributs.forEach {
            attrs.add(AttributeProperty(it))

            if (it.type == AttributeType.OTHER) {
                if (others.isNotEmpty()) others += ", "
                others += "\"${it.name}\""
            }
        }

        // Liste des attributs
        val fields = mutableMapOf<String,VariableElement>()
        element.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD) fields[it.simpleName.toString()] = it as VariableElement
        }

        // Analyse des annotations attributs
        element.enclosedElements.forEach {
            val name = it.simpleName.split("$").first()

            // Annotations
            val unif = it.getAnnotation(Uniform::class.java)
            val ssbo = it.getAnnotation(SharedStorage::class.java)
            val unifBlock = it.getAnnotation(UniformBlock::class.java)

            // Attributs & Uniforms
            if (unif != null) {
                val prop = UniformProperty(fields[name]!!, unif)

                if (unif.meshMaterial) {
                    meshMaterialAttribute = prop
                } else {
                    unifs.add(prop)
                }
            } else if (unifBlock != null) {
                if (unifBlock.name == "") {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "UniformBlock name is required for attributes")
                }

                unifBlocks.add(UniformBlockProperty(fields[name]!!, unifBlock))
            } else if (ssbo != null) {
                if (ssbo.name == "") {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "SharedStorageBlock name is required for attributes")
                }

                ssbos.add(ShaderStorageProperty(fields[name]!!, ssbo))
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
                                    "GLES32.glAttachShader(program, loadShaderAsset(context, %S, %T.%L))",
                                    shader.file,
                                    ShaderType::class, shader.type.name
                            )
                        } else if (shader.script != "") {
                            addStatement(
                                    "GLES32.glAttachShader(program, loadShader(%S, %T.%L))",
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
                        for (prop in unifBlocks) {
                            addStatement("bindUniformBlock(%S, ${prop.name}.binding)", prop.annotation.name)
                        }
                    }

                    for (prop in ssbos) addStatement("${prop.name}.binding = bindSharedStorage(%S)", prop.annotation.name)
                }.build()

        val genBuffers = FunSpec.builder("genBuffers")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in unifBlocks) addStatement("${prop.name}.generate()")
                    for (prop in ssbos)      addStatement("${prop.name}.generate()")
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

                    for (prop in unifBlocks) addStatement("${prop.name}.load()")
                    for (prop in ssbos)      addStatement("${prop.name}.load()")
                }.build()

        val enableVBO = FunSpec.builder("enableVBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("mesh", ClassName("net.capellari.julien.opengl.base", "BaseMesh"))

                    // Link to arrays
                    beginControlFlow("mesh.bindVBO")
                    for (prop in attrs) {
                        when (prop.annotation.type) {
                            AttributeType.VERTICES -> {
                                addStatement("GLES32.glEnableVertexAttribArray(%N)", prop.handle)
                                addStatement("GLES32.glVertexAttribPointer(%N, 3, mesh.vertexType, %L, 0, 0)", prop.handle, prop.annotation.normalized)
                            }
                            AttributeType.NORMALS -> {
                                beginControlFlow("if (mesh.hasNormals)")
                                    addStatement("GLES32.glEnableVertexAttribArray(%N)", prop.handle)
                                    addStatement("GLES32.glVertexAttribPointer(%N, 3, mesh.normalType, %L, 0, mesh.vertexSize)", prop.handle, prop.annotation.normalized)
                                endControlFlow()
                            }
                            AttributeType.TEXCOORDS -> {
                                beginControlFlow("if (mesh.hasTexCoords)")
                                    addStatement("GLES32.glEnableVertexAttribArray(%N)", prop.handle)
                                    addStatement("GLES32.glVertexAttribPointer(%N, 2, mesh.texCoordType, %L, 0, mesh.vertexSize + mesh.normalSize)", prop.handle, prop.annotation.normalized)
                                endControlFlow()
                            }
                            AttributeType.OTHER -> {
                                addStatement("GLES32.glEnableVertexAttribArray(%N)", prop.handle)
                                addStatement("GLES32.glVertexAttribPointer(%N, 3, mesh.othersType[%S]!!, %L, 0, mesh.othersOff[%S]!!)",
                                        prop.handle, prop.annotation.name, prop.annotation.normalized, prop.annotation.name)
                            }
                        }
                    }
                    endControlFlow()
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

                // Contructeur
                addInitializerBlock(CodeBlock.builder()
                    .apply {
                        addStatement("otherAttrs = arrayOf($others)")

                        if (program.mode != 0) {
                            addStatement("mode = %L", program.mode)
                            addStatement("defaultMode = %L", program.mode)
                        }
                    }.build()
                )

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
                    addImport("android.opengl", "GLES32")
                    addImport("android.util", "Log")
                    addImport("net.capellari.julien.opengl", "GLUtils")

                    addType(cls)
                }.build()

        // Create file
        Utils.writeTo(sourceRoot, code)
    }
    private fun createUniformBlockImpl(element: TypeElement) {
        // Get infos
        val pkgProgram = processingEnv.elementUtils.getPackageOf(element).toString()

        // Classes
        val baseCls = ClassName(pkgProgram, element.simpleName.toString())
        val implCls = ClassName(pkgProgram, "${element.simpleName}_Impl")

        // Liste des attributs
        val fields = mutableMapOf<String,VariableElement>()
        element.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD) fields[it.simpleName.toString()] = it as VariableElement
        }

        // Champs
        val champs = mutableListOf<VariableElement>()
        element.enclosedElements.forEach {
            val name = it.simpleName.split("$").first()

            // Annotations
            val field = it.getAnnotation(Field::class.java)

            if (field != null) {
                champs.add(fields[name]!!)
            }
        }

        // Implémentation de la classe
        val cls = TypeSpec.classBuilder(implCls)
            .apply {
                superclass(baseCls)

                // Propriétés
                champs.forEach {
                    val name = it.simpleName.toString()
                    val type = it.asType().asTypeName()

                    val param = ParameterSpec.builder("value", type).build()

                    addProperty(PropertySpec.builder(name, type, KModifier.OVERRIDE)
                        .apply {
                            mutable()
                            initializer("super.$name")

                            setter(FunSpec.builder("set()").apply {
                                addParameter(param)

                                addStatement("field = %N", param)
                                addStatement("ubo.reload = true")
                            }.build())
                        }.build()
                    )
                }

                // Méthodes
                addFunction(FunSpec.builder("load")
                    .apply {
                        addModifiers(KModifier.OVERRIDE)

                        beginControlFlow("if (ubo.reload)")
                            // Compute ssbo size
                            addStatement("var size = 0")
                            champs.forEach {
                                addStatement("size += GLUtils.bufferSize(${it.simpleName})")
                            }

                            // Allocate UBO
                            addStatement("ubo.allocate(size)")
                            addStatement("ubo.position = 0")

                            // Put data
                            champs.forEach {
                                addStatement("ubo.put(${it.simpleName})")
                            }

                            // Bind UBO
                            addStatement("ubo.toGPU()")
                            addStatement("GLUtils.checkGlError(%S)", "Loading uniform block ${baseCls.simpleName}")

                            addStatement("ubo.reload = false")
                        endControlFlow()
                    }.build()
                )
            }.build()

        // Ecriture
        val code = FileSpec.builder(pkgProgram, implCls.simpleName)
                .apply {
                    addImport("net.capellari.julien.opengl", "GLUtils")

                    addType(cls)
                }.build()

        // Create file
        Utils.writeTo(sourceRoot, code)
    }
    private fun createSharedStorageBlockImpl(element: TypeElement) {
        // Get infos
        val pkgProgram = processingEnv.elementUtils.getPackageOf(element).toString()

        // Classes
        val baseCls = ClassName(pkgProgram, element.simpleName.toString())
        val implCls = ClassName(pkgProgram, "${element.simpleName}_Impl")

        // Liste des attributs
        val fields = mutableMapOf<String,VariableElement>()
        element.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD) fields[it.simpleName.toString()] = it as VariableElement
        }

        // Champs
        val champs = mutableListOf<VariableElement>()
        element.enclosedElements.forEach {
            val name = it.simpleName.split("$").first()

            // Annotations
            val field = it.getAnnotation(Field::class.java)

            if (field != null) {
                champs.add(fields[name]!!)
            }
        }

        // Implémentation de la classe
        val cls = TypeSpec.classBuilder(implCls)
            .apply {
                superclass(baseCls)

                // Propriétés
                champs.forEach {
                    val name = it.simpleName.toString()
                    val type = it.asType().asTypeName()

                    val param = ParameterSpec.builder("value", type).build()

                    addProperty(PropertySpec.builder(name, type, KModifier.OVERRIDE)
                        .apply {
                            mutable()
                            initializer("super.$name")

                            setter(FunSpec.builder("set()").apply {
                                addParameter(param)

                                addStatement("field = %N", param)
                                addStatement("ssbo.reload = true")
                            }.build())
                        }.build()
                    )
                }

                // Méthodes
                addFunction(FunSpec.builder("load")
                    .apply {
                        addModifiers(KModifier.OVERRIDE)

                        beginControlFlow("if (ssbo.reload)")
                            // Compute ssbo size
                            addStatement("var size = 0")
                            champs.forEach {
                                addStatement("size += GLUtils.bufferSize(${it.simpleName})")
                            }

                            // Allocate SSBO
                            addStatement("ssbo.allocate(size)")
                            addStatement("ssbo.position = 0")

                            // Put data
                            champs.forEach {
                                addStatement("ssbo.put(${it.simpleName})")
                            }

                            // Bind SSBO
                            addStatement("ssbo.toGPU()")
                            addStatement("GLUtils.checkGlError(%S)", "Loading shared storage block ${baseCls.simpleName}")

                            addStatement("ssbo.reload = false")
                        endControlFlow()
                    }.build()
                )
            }.build()

        // Ecriture
        val code = FileSpec.builder(pkgProgram, implCls.simpleName)
                .apply {
                    addImport("net.capellari.julien.opengl", "GLUtils")

                    addType(cls)
                }.build()

        // Create file
        Utils.writeTo(sourceRoot, code)
    }
}