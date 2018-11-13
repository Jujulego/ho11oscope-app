package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement

internal abstract class HandleProperty(element: VariableElement) : BaseProperty() {
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
    protected abstract fun createProperty()
    abstract fun getLocationFunc(func: FunSpec.Builder)

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