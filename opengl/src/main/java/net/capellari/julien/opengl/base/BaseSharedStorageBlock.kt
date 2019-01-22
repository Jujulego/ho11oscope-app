package net.capellari.julien.opengl.base

import net.capellari.julien.opengl.buffers.ShaderStorageBufferObject
import kotlin.reflect.full.createInstance

abstract class BaseSharedStorageBlock {
    // Companion
    companion object {
        // Attributs
        const val TAG = "BaseSharedStorageBlock"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : BaseSharedStorageBlock> getImplementation() : T =
                Class.forName("${T::class.qualifiedName}_Impl").kotlin.createInstance() as T
    }

    // Attributs
    protected val ssbo = ShaderStorageBufferObject()
    var binding get() = ssbo.binding
        set(value) {
            ssbo.binding = value
        }

    // Méthodes
    fun generate() { ssbo.generate() }
    abstract fun load()
}