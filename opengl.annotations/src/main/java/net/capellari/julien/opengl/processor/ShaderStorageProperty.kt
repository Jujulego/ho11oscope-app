package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.ShaderStorage

internal class ShaderStorageProperty(val annotation: ShaderStorage) : BufferProperty(annotation.name) {
    // Companion
    companion object {
        val SSBOClass = ClassName("net.capellari.julien.opengl.buffers", "ShaderStorageBufferObject")
    }

    // Attributs
    override val log: String
        get() = "shader storage ${annotation.name}"

    override val bufferType: String
        get() = "GLES31.GL_SHADER_STORAGE_BUFFER"

    // Méthodes
    override fun createBO(name: String) {
        bo = PropertySpec.builder("ssbo$name", SSBOClass, KModifier.PRIVATE)
                .apply {
                    initializer("ShaderStorageBufferObject()")
                }.build()
    }

    override fun bindFunc(func: FunSpec.Builder) {
        func.addStatement("%N = bindSharedStorage(%S)", binding, annotation.name)
    }
}