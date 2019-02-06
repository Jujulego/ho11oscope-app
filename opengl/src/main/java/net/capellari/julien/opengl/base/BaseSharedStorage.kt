package net.capellari.julien.opengl.base

import net.capellari.julien.opengl.buffers.ShaderStorageBufferObject
import kotlin.reflect.full.createInstance

abstract class BaseSharedStorage {
    // Companion
    companion object {
        // Attributs
        const val TAG = "BaseSharedStorage"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : BaseSharedStorage> getImplementation() : T =
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