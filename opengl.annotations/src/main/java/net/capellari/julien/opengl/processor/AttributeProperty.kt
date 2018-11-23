package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import net.capellari.julien.opengl.Attribute
import javax.lang.model.element.VariableElement

internal class AttributeProperty(val annotation: Attribute) : BaseProperty() {
    // Attributs
    lateinit var handle: PropertySpec
        protected set

    // Constructeur
    init {
        createHandle()
    }

    // Méthodes
    fun getLocationFunc(func: FunSpec.Builder) {
        func.addStatement("%N = getAttribLocation(%S)", handle, annotation.name)
    }

    private fun createHandle() {
        handle = PropertySpec.builder("${annotation.name}Handle", Int::class, KModifier.PRIVATE)
                .initializer("GLES31.GL_INVALID_INDEX").mutable()
                .build()
    }

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperty(handle)
    }
}