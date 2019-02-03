package net.capellari.julien.opengl.base

import net.capellari.julien.opengl.buffers.ShaderStorageBufferObject
import kotlin.reflect.full.createInstance

abstract class SharedStorageBlock {
    // Companion
    companion object {
        // Attributs
        const val TAG = "SharedStorageBlock"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : SharedStorageBlock> getImplementation() : T =
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