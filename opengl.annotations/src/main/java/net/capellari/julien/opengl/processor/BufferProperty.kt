package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement

@RequiresApi(26)
internal abstract class BufferProperty(name: String) : BaseProperty() {
    // Attributs
    abstract val log: String
    abstract val bufferType: String

    val properties = ArrayList<PropertySpec>()

    val id = PropertySpec.builder("id$name", Int::class, KModifier.PRIVATE)
            .apply {
                mutable()
                initializer("GLES31.GL_INVALID_INDEX")
            }.build()

    val reload = PropertySpec.builder("reload$name", Boolean::class, KModifier.PRIVATE)
            .apply {
                mutable()
                initializer("true")
            }.build()

    val binding = PropertySpec.builder("binding$name", Int::class, KModifier.PRIVATE)
            .apply {
                mutable()
                initializer("GLES31.GL_INVALID_INDEX")
            }.build()

    lateinit var bo: PropertySpec
        protected set

    // Construteur
    init {
        this.createBO(name)
    }

    // Méthodes
    protected abstract fun createBO(name: String)
    abstract fun bindFunc(func: FunSpec.Builder)

    fun add(element: VariableElement) {
        val name = element.simpleName.toString()
        val type = Utils.getTypeName(element.asType().asTypeName())
        val param = ParameterSpec.builder("value", type).build()

        properties.add(PropertySpec.builder(name, type, KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("%N = true", reload)
                    }.build())
                }.build())
    }

    fun genBufferFunc(func: FunSpec.Builder) {
        func.addStatement("%N = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]", id)
    }

    fun loadBufferFunc(func: FunSpec.Builder) {
        func.beginControlFlow("if (%N)", reload)
            // Compute ssbo size
            func.addStatement("var size = 0")
            for (p in properties) {
                func.addStatement("size += GLUtils.bufferSize(%N)", p)
            }

            // Allocate SSBO
            func.addStatement("%N.allocate(size)", bo)
            func.addStatement("%N.position = 0", bo)

            // Put data
            for (p in properties) {
                func.addStatement("%N.put(%N)", bo, p)
            }

            // Bind SSBO
            func.addStatement("%N.bind(%N)", bo, id)
            func.addStatement("GLES31.glBindBufferBase($bufferType, %N, %N)", binding, id)
            func.addStatement("GLUtils.checkGlError(%S)", "Loading $log")

            func.addStatement("%N = false", reload)
        func.endControlFlow()
    }

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperties(properties)
        type.addProperty(id)
        type.addProperty(reload)
        type.addProperty(binding)
        type.addProperty(bo)
    }
}