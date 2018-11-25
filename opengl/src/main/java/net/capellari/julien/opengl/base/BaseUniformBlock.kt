package net.capellari.julien.opengl.base

import net.capellari.julien.opengl.buffers.UniformBufferObject
import kotlin.reflect.full.createInstance

abstract class BaseUniformBlock {
    // Companion
    companion object {
        // Attributs
        const val TAG = "BaseUniformBlock"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : BaseUniformBlock> getImplementation() : T =
                Class.forName("${T::class.qualifiedName}_Impl").kotlin.createInstance() as T
    }

    // Attributs
    protected val ubo = UniformBufferObject()
    val binding get() = ubo.binding

    // Méthodes
    fun generate() { ubo.generate() }
    abstract fun load()
}