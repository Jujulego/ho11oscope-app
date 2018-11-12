package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.UniformBlock


class UniformBlockProperty(val annotation: UniformBlock) : BufferProperty(annotation.name) {
    // Companion
    companion object {
        val UBOClass = ClassName("net.capellari.julien.opengl.buffers", "UniformBufferObject")
    }

    // Méthodes
    override fun createBO(name: String) {
        bo = PropertySpec.builder("ubo$name", UBOClass, KModifier.PRIVATE)
                .apply {
                    initializer("UniformBufferObject()")
                }.build()
    }
}