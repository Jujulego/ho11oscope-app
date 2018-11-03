package net.capellari.julien.opengl

import android.opengl.GLES20
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import net.capellari.julien.opengl.base.BufferObject
import java.lang.RuntimeException
import java.nio.*
import kotlin.reflect.KClass

class VertexBufferObject : BufferObject(GLES20.GL_ARRAY_BUFFER) {
    // Put
    // - bases
    fun put(value: Short) {
        buffer?.putShort(value)
    }
    fun put(array: ShortArray) {
        array.forEach { put(it) }
    }
    fun put(buffer: ShortBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(value: Int) {
        buffer?.putInt(value)
    }
    fun put(array: IntArray) {
        array.forEach { put(it) }
    }
    fun put(buffer: IntBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(value: Float) {
        buffer?.putFloat(value)
    }
    fun put(array: FloatArray) {
        array.forEach { put(it) }
    }
    fun put(buffer: FloatBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(vec: BaseVec<*>) {
        put(vec.data)
    }
    fun put(vec: BaseMat<*,*>) {
        put(vec.data)
    }

    // - generics
    inline fun<reified T : Any> put(collection: Collection<T>) {
        for (value in collection) put(value)
    }

    inline fun<reified T : Any> put(collection: Array<T>) {
        for (value in collection) put(value)
    }

    inline fun<reified T : Any> put(value: T) {
        when (value) {
            // Base
            is Short -> put(value as Short)
            is Int   -> put(value as Int)
            is Float -> put(value as Float)

            // Vecteurs
            is BaseVec<*> -> put(value as BaseVec<*>)

            // Matrices
            is BaseMat<*,*> -> put(value as BaseMat<*,*>)

            else -> throw RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }
}