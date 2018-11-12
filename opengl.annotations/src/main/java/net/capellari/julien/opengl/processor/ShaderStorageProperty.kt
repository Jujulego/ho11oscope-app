package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.ShaderStorage

class ShaderStorageProperty(val annotation: ShaderStorage) : BufferProperty(annotation.name) {
    // Companion
    companion object {
        val SSBOClass = ClassName("net.capellari.julien.opengl.buffers", "ShaderStorageBufferObject")
    }

    // Méthodes
    override fun createBO(name: String) {
        bo = PropertySpec.builder("ssbo$name", SSBOClass, KModifier.PRIVATE)
                .apply {
                    initializer("ShaderStorageBufferObject()")
                }.build()
    }
}