package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement

abstract class BufferProperty(name: String) : BaseProperty() {
    // Attributs
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

    fun add(element: VariableElement) {
        val name = element.simpleName.toString()
        val type = element.asType().asTypeName()
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

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperties(properties)
        type.addProperty(id)
        type.addProperty(reload)
        type.addProperty(binding)
        type.addProperty(bo)
    }
}