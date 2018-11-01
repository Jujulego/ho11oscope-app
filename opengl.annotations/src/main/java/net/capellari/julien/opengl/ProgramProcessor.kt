package net.capellari.julien.opengl

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ProgramProcessor : AbstractProcessor() {
    // Propriétés
    private val sourceRoot get() = processingEnv.options["kapt.kotlin.generated"]

    // Classes
    inner class UniformProperty(element: VariableElement, val annotation: Uniform) {
        // Attributs
        lateinit var handle: PropertySpec
            private set

        lateinit var property: PropertySpec
            private set

        val name = element.simpleName.toString()
        val type = element.asType().asTypeName()

        // Constructeur
        init {
            createHandle()
            createProperty()

            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, element.simpleName)
        }

        // Méthodes
        private fun createHandle() {
            handle = PropertySpec.builder("${name}Handle", Int::class, KModifier.PRIVATE)
                        .initializer("-1").mutable()
                        .build()
        }

        private fun createProperty() {
            val param = ParameterSpec.builder("value", type).build()

            property = PropertySpec.builder(name, type, KModifier.OVERRIDE)
                    .apply {
                        mutable()
                        initializer("super.$name")

                        setter(FunSpec.builder("set()").apply {
                            addParameter(param)

                            addStatement("field = %N", param)
                            addStatement("setUniformValue(%N, %N)", handle, param)
                            addStatement("GLUtils.checkGlError(%S)", "Loading uniform $name")
                            //addStatement("Log.d(%S, %S)", "BaseProgram", "Loaded uniform $name")
                        }.build())
                    }.build()

        }
    }

    // Méthodes
    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
                Program::class.java.canonicalName
        )
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        // Check if classes has both vertex and fragment shader
        roundEnv.getElementsAnnotatedWith(Program::class.java)
                .forEach {
                    shadersCheck(it as TypeElement)
                    createProgramImpl(it)
                }

        return false
    }

    // - tests
    private fun shadersCheck(element: TypeElement) {
        // Get annotation
        val program = element.getAnnotation(Program::class.java)
        var hasFragment = false
        var hasVertex = false

        // Check Scripts
        program.shaders.forEach {
            if ((it.file != "") or (it.script != "")) {
                when (it.type) {
                    ShaderType.FRAGMENT -> hasFragment = true
                    ShaderType.VERTEX -> hasVertex = true
                }
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "One ${element.simpleName}'s ShaderScript must have a file or a script")
            }
        }

        // Check consistency
        if (!hasFragment) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${element.simpleName} has no fragment shader")
        }

        if (!hasVertex) {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${element.simpleName} has no vertex shader")
        }
    }

    private fun createProgramImpl(element: TypeElement) {
        // Get infos
        val pkgProgram = processingEnv.elementUtils.getPackageOf(element).toString()
        val program = element.getAnnotation(Program::class.java)

        // Classes
        val baseCls = ClassName(pkgProgram, element.simpleName.toString())
        val implCls = ClassName(pkgProgram, "${element.simpleName}_Impl")

        // Attributes
        val attrs = mutableMapOf<PropertySpec,Attribute>()
        val unifs = mutableListOf<UniformProperty>()
        var ibo: Pair<String,IBO>? = null
        var vbo: Pair<String,VBO>? = null


        // liste des attributs
        val fields = mutableMapOf<String,VariableElement>()
        element.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD) fields[it.simpleName.toString()] = it as VariableElement
        }

        element.enclosedElements
                .forEach {
                    // Annotations
                    val attr = it.getAnnotation(Attribute::class.java)
                    val unif = it.getAnnotation(Uniform::class.java)

                    // Attributs & Uniforms
                    if (attr != null) {
                        val p = PropertySpec.builder("${it.simpleName.split('$').first()}Handle", Int::class, KModifier.PRIVATE)
                                .initializer("-1").mutable()
                                .build()

                        attrs[p] = attr
                    } else if (unif != null) {
                        unifs.add(UniformProperty(fields[it.simpleName.split("$").first()]!!, unif))
                    }

                    // VBO !
                    it.getAnnotation(VBO::class.java)?.let { annot ->
                        // Doublons ?
                        vbo?.let { vbo ->
                            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING,
                                    "@VBO used twice on the same program ('${vbo.first}' and '${it.simpleName}')"
                            )
                        }

                        // Paire !
                        vbo = it.simpleName.toString() to annot
                    }

                    // IBO !
                    it.getAnnotation(IBO::class.java)?.let { annot ->
                        // Doublons ?
                        ibo?.let { ibo ->
                            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING,
                                    "@IBO used twice on the same program ('${ibo.first}' and '${it.simpleName}')"
                            )
                        }

                        // Paire !
                        ibo = it.simpleName.toString() to annot
                    }
                }

        // Méthodes
        val loadShaders = FunSpec.builder("loadShaders")
                .apply {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("context", ClassName("android.content", "Context"))
                    addParameter("program", Int::class)

                    for (shader in program.shaders) {
                        if (shader.file != "") {
                            addStatement(
                                    "GLES20.glAttachShader(program, loadShaderAsset(context, %S, %T.%L))",
                                    shader.file,
                                    ShaderType::class, shader.type.name
                            )
                        } else if (shader.script != "") {
                            addStatement(
                                    "GLES20.glAttachShader(program, loadShader(%S, %T.%L))",
                                    shader.script,
                                    ShaderType::class, shader.type.name
                            )
                        }
                    }
                }.build()

        val getLocations = FunSpec.builder("getLocations")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (p in attrs) {
                        addStatement("%N = getAttribLocation(%S)", p.key, p.value.name)
                    }

                    for (prop in unifs) {
                        addStatement("%N = getUniformLocation(%S)", prop.handle, prop.annotation.name)
                    }
                }.build()

        val loadUniforms = FunSpec.builder("loadUniforms")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in unifs) {
                        addStatement("setUniformValue(%N, %N)", prop.handle, prop.property)
                        addStatement("GLUtils.checkGlError(%S)", "Loading uniform ${prop.annotation.name}")
                    }
                }.build()

        val loadIBO = FunSpec.builder("loadIBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    ibo?.let { ibo ->
                        addCode("""
                            |iboId = ${ibo.first}?.let {
                            |   it.position(0)
                            |
                            |   IntArray(1).also { id ->
                            |       GLES20.glGenBuffers(1, id, 0)
                            |
                            |       GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, id[0])
                            |       GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                            |               it.capacity() * ${ibo.second.type.size},
                            |               it, GLES20.GL_STATIC_DRAW
                            |       )
                            |   }[0]
                            |} ?: -1
                            |""".trimMargin())
                    }
                }.build()

        val loadVBO = FunSpec.builder("loadVBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    vbo?.also { vbo ->
                        // Enabling part
                        val sizeCode = CodeBlock.builder()
                                .addStatement("var size = 0")

                        val putCode = CodeBlock.builder()
                                .indent().indent().indent()

                        val enableCode = CodeBlock.builder()
                                .indent().indent().addStatement("var offset = 0")

                        for (p in attrs) {
                            if (p.value.vbo > 0) {
                                val name = p.key.name.substring(0, p.key.name.length - 6)

                                sizeCode
                                    .beginControlFlow("%L?.let", name)
                                        .addStatement("size += it.capacity() * %L", vbo.second.type.size)
                                    .endControlFlow()

                                putCode
                                    .beginControlFlow("%L?.let", name)
                                        .addStatement("it.position(0)")
                                        .addStatement("put(it)")
                                    .endControlFlow()

                                enableCode
                                    .beginControlFlow("%L?.let", name)
                                        .addStatement("GLES20.glEnableVertexAttribArray(%N)", p.key)
                                        .addStatement("GLES20.glVertexAttribPointer(%N, %L, GLES20.GL_%L, false, 0, offset)",
                                                p.key, p.value.vbo, vbo.second.type.name)

                                        .addStatement("offset += it.capacity() * %L", vbo.second.type.size)
                                    .endControlFlow()
                            }
                        }

                        putCode.unindent().unindent().unindent()
                        enableCode.unindent().unindent()

                        // Final code
                        addCode(CodeBlock.builder()
                            .apply {
                                addCode(sizeCode.build())
                                addCode("""|
                                    |vbo = if (size > 0) ByteBuffer.allocateDirect(size)
                                    |        .order(ByteOrder.nativeOrder())
                                    |        .asFloatBuffer().apply {
                                    |            position(0)
                                    |""".trimMargin())
                                addCode(putCode.build())
                                addCode("""|            position(0)
                                    |        } else null
                                    |
                                    |vboId = ${vbo.first}?.let {
                                    |    IntArray(1).also { id ->
                                    |        GLES20.glGenBuffers(1, id, 0)
                                    |
                                    |""".trimMargin())
                                addCode("""
                                    |        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, id[0])
                                    |        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size, it, GLES20.GL_STATIC_DRAW)
                                    |
                                    |""".trimMargin())
                                addCode(enableCode.build())
                                addCode("""|
                                    |    }[0]
                                    |} ?: -1
                                    |""".trimMargin())
                            }.build())
                    }
                }.build()

        val clean = FunSpec.builder("clean")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (p in attrs) {
                        addStatement("GLES20.glDisableVertexAttribArray(%N)", p.key)
                    }
                }.build()

        // Implementation de la classe
        val cls = TypeSpec.classBuilder(implCls)
            .apply {
                superclass(baseCls)

                // Attributs
                addProperties(attrs.keys)
                for (prop in unifs) {
                    addProperty(prop.handle)
                    addProperty(prop.property)
                }

                // Méthodes
                addFunction(loadShaders)
                addFunction(getLocations)

                addFunction(loadUniforms)
                addFunction(loadIBO)
                addFunction(loadVBO)

                addFunction(clean)
            }

        // Ecriture
        val file = File(sourceRoot)
        file.mkdir()

        FileSpec.builder(pkgProgram, implCls.simpleName)
                .apply {
                    addImport("android.opengl", "GLES20")
                    addImport("net.capellari.julien.opengl", "GLUtils")
                    addImport("java.nio", "ByteBuffer", "ByteOrder")
                    addImport("android.util", "Log")
                    vbo?.also {
                        val bcls = it.second.type.cls.asClassName()
                        addImport(bcls.packageName, bcls.simpleName)
                    }

                    addType(cls.build())
                }.build().writeTo(file)
    }
}