package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import net.capellari.julien.opengl.Uniform
import javax.lang.model.element.VariableElement

internal class UniformProperty(element: VariableElement, val annotation: Uniform) : BaseProperty() {
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
    fun createProperty() {
        val param = ParameterSpec.builder("value", type).build()

        property = PropertySpec.builder(name, type, KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("reloadUniforms = true")
                    }.build())
                }.build()
    }

    fun getLocationFunc(func: FunSpec.Builder) {
        func.addStatement("%N = getUniformLocation(%S)", handle, annotation.name)
    }

    private fun createHandle() {
        handle = PropertySpec.builder("${name}Handle", Int::class, KModifier.PRIVATE)
                .initializer("GLES31.GL_INVALID_INDEX").mutable()
                .build()
    }

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperty(handle)
        type.addProperty(property)
    }
}