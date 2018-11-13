package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.UniformBlock

internal class UniformBlockProperty(val annotation: UniformBlock) : BufferProperty(annotation.name) {
    // Companion
    companion object {
        val UBOClass = ClassName("net.capellari.julien.opengl.buffers", "UniformBufferObject")
    }

    // Attributs
    override val log: String
        get() = "uniform block ${annotation.name}"

    override val bufferType: String
        get() = "GLES31.GL_UNIFORM_BUFFER"

    // Méthodes
    override fun createBO(name: String) { // Pas accès à annotation
        bo = PropertySpec.builder("ubo$name", UBOClass, KModifier.PRIVATE)
                .apply {
                    initializer("UniformBufferObject()")
                }.build()
    }

    override fun bindFunc(func: FunSpec.Builder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}