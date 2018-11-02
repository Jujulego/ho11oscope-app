package net.capellari.julien.opengl

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
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
    abstract inner class HandleProperty(element: VariableElement) {
        // Attributs
        lateinit var handle: PropertySpec
            protected set

        lateinit var property: PropertySpec
            protected set

        val name = element.simpleName.toString()
        val type = element.asType().asTypeName()

        // Constructeur
        init {
            createHandle()
            this.createProperty()
        }

        // Méthodes
        private fun createHandle() {
            handle = PropertySpec.builder("${name}Handle", Int::class, KModifier.PRIVATE)
                    .initializer("-1").mutable()
                    .build()
        }
        protected abstract fun createProperty()
    }
    inner class UniformProperty(element: VariableElement, val annotation: Uniform) : HandleProperty(element) {
        // Méthodes
        override fun createProperty() {
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
    inner class AttributeProperty(element: VariableElement, val annotation: Attribute) : HandleProperty(element) {
        // Attributs
        val vbo = element.getAnnotation(VBO::class.java) != null

        // Méthodes
        override fun createProperty() {
            val param = ParameterSpec.builder("value", type).build()

            property = PropertySpec.builder(name, type.asNullable(), KModifier.OVERRIDE)
                    .apply {
                        mutable()
                        initializer("super.$name")

                        setter(FunSpec.builder("set()").apply {
                            addParameter(param)

                            addStatement("field = %N", param)
                            addStatement("reloadVBO = true")
                        }.build())
                    }.build()
        }
    }
    inner class IndicesProperty(element: VariableElement, val annotation: IBO) {
        // Attributs
        lateinit var property: PropertySpec
            private set

        val name = element.simpleName.toString()
        val type = element.asType().asTypeName().asNullable()

        // Constructeur
        init {
            createProperty()
        }

        // Méthodes
        private fun createProperty() {
            val param = ParameterSpec.builder("value", type).build()

            property = PropertySpec.builder(name, type, KModifier.OVERRIDE)
                    .apply {
                        mutable()
                        initializer("super.$name")

                        setter(FunSpec.builder("set()").apply {
                            addParameter(param)

                            addStatement("field = %N", param)
                            beginControlFlow("if (iboId != -1)")
                                beginControlFlow("%N?.also", param)
                                    addStatement("it.position(0)")

                                    beginControlFlow("usingProgram")
                                        addStatement("GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId)")
                                        addStatement("GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferSize(it), it, GLES20.GL_STATIC_DRAW)")
                                        addStatement("GLUtils.checkGlError(\"Loading ibo\")")
                                    endControlFlow()
                                endControlFlow()
                            endControlFlow()
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
        val attrs = mutableListOf<AttributeProperty>()
        val unifs = mutableListOf<UniformProperty>()
        var ibo: IndicesProperty? = null

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
                    val attr = it.getAnnotation(Attribute::class.java)
                    val unif = it.getAnnotation(Uniform::class.java)

                    // Attributs & Uniforms
                    if (attr != null) {
                        attrs.add(AttributeProperty(fields[name]!!, attr))
                    } else if (unif != null) {
                        unifs.add(UniformProperty(fields[name]!!, unif))
                    }

                    // IBO !
                    it.getAnnotation(IBO::class.java)?.let { annot ->
                        // Doublons ?
                        ibo?.let { ibo ->
                            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING,
                                    "@IBO used twice on the same program ('${ibo.name}' and '$name')"
                            )
                        }

                        // Paire !
                        ibo = IndicesProperty(fields[name]!!, annot)
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

                    for (prop in attrs) {
                        addStatement("%N = getAttribLocation(%S)", prop.handle, prop.annotation.name)
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

        val loadVBO = FunSpec.builder("loadVBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    // Compute vbo size
                    addStatement("var size = 0")
                    for (prop in attrs) {
                        if (prop.vbo) {
                            beginControlFlow("${prop.name}?.let")
                                addStatement("size += bufferSize(it)")
                            endControlFlow()
                        }
                    }

                    // Allocate VBO
                    addStatement("vbo.allocate(size)")
                    addStatement("vbo.position = 0")

                    // Put data
                    for (prop in attrs) {
                        if (prop.vbo) {
                            beginControlFlow("${prop.name}?.let")
                                addStatement("vbo.put(it)")
                            endControlFlow()
                        }
                    }

                    // Bind VBO
                    addStatement("vbo.bind(vboId)")
                }.build()

        val enableVBO = FunSpec.builder("enableVBO")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    // Link to arrays
                    addStatement("var offset = 0")

                    for (prop in attrs) {
                        if (prop.vbo) {
                            beginControlFlow("${prop.name}?.let")
                                addStatement("GLES20.glEnableVertexAttribArray(%N)", prop.handle)
                                addStatement("GLES20.glVertexAttribPointer(%N, numberComponents(it), bufferType(it), false, 0, offset)", prop.handle)

                                addStatement("offset += bufferSize(it)")
                            endControlFlow()
                        }
                    }
                }.build()

        val draw = FunSpec.builder("draw")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    ibo?.also { ibo ->
                        beginControlFlow("%N?.also {", ibo.property)
                            addStatement("GLES20.glDrawElements(GLES20.GL_TRIANGLES, it.capacity(), bufferType(it, true), 0)",
                                    BufferType::class)
                            addStatement("GLUtils.checkGlError(\"Drawing\")")
                        endControlFlow()
                    }
                }.build()

        val clean = FunSpec.builder("clean")
                .apply {
                    addModifiers(KModifier.OVERRIDE)

                    for (prop in attrs) {
                        addStatement("GLES20.glDisableVertexAttribArray(%N)", prop.handle)
                    }
                }.build()

        // Implementation de la classe
        val cls = TypeSpec.classBuilder(implCls)
            .apply {
                superclass(baseCls)

                // Attributs
                ibo?.also { ibo -> addProperty(ibo.property) }
                for (prop in attrs) {
                    addProperty(prop.handle)
                    addProperty(prop.property)
                }
                for (prop in unifs) {
                    addProperty(prop.handle)
                    addProperty(prop.property)
                }

                // Méthodes
                addFunction(loadShaders)
                addFunction(getLocations)

                addFunction(loadUniforms)
                addFunction(loadVBO)
                addFunction(enableVBO)

                addFunction(draw)
                addFunction(clean)
            }

        // Ecriture
        val code = FileSpec.builder(pkgProgram, implCls.simpleName)
                .apply {
                    addImport("android.opengl", "GLES20")
                    addImport("android.util", "Log")
                    addImport("net.capellari.julien.opengl", "GLUtils")

                    addType(cls.build())
                }.build()

        // Create file
        var output = Paths.get(sourceRoot)

        if (!Files.exists(output)) Files.createDirectory(output)
        if (code.packageName.isNotEmpty()) {
            for (packageComponent in code.packageName.split('.').dropLastWhile { it.isEmpty() }) {
                output = output.resolve(packageComponent)
            }
        }

        Files.createDirectories(output)
        output = output.resolve("${code.name}.kt")

        // Write down to file
        val writer = OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8)

        code.writeTo(object : Appendable {
            fun replace(str: CharSequence?): CharSequence {
                return (str ?: "null").replace("java\\.lang".toRegex(), "kotlin")
            }

            override fun append(str: CharSequence?): java.lang.Appendable {
                writer.append(replace(str))
                return this
            }

            override fun append(str: CharSequence?, p1: Int, p2: Int): java.lang.Appendable {
                writer.append(replace(str), p1, p2)
                return this
            }

            override fun append(c: Char): java.lang.Appendable {
                writer.append(c)
                return this
            }
        })

        writer.close()
    }
}