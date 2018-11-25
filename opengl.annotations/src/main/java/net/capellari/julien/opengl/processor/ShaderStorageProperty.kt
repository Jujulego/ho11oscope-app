package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.ShaderStorage

@RequiresApi(26)
internal class ShaderStorageProperty(val annotation: ShaderStorage) : BufferProperty(annotation.name) {
    // Companion
    companion object {
        val SSBOClass = ClassName("net.capellari.julien.opengl.buffers", "ShaderStorageBufferObject")
    }

    // Attributs
    override val log: String
        get() = "shader storage ${annotation.name}"

    // Méthodes
    override fun createBO(name: String) {
        bo = PropertySpec.builder("ssbo$name", SSBOClass, KModifier.PRIVATE)
                .apply {
                    initializer("ShaderStorageBufferObject()")
                }.build()
    }

    override fun bindFunc(func: FunSpec.Builder) {
        func.addStatement("%N.binding = bindSharedStorage(%S)", bo, annotation.name)
    }
}