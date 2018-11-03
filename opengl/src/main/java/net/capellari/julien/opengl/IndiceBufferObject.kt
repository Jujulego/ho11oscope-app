package net.capellari.julien.opengl

import android.opengl.GLES20
import net.capellari.julien.opengl.base.BufferObject
import java.lang.RuntimeException
import java.nio.IntBuffer
import java.nio.ShortBuffer

class IndiceBufferObject : BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER) {
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

            else -> throw RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }
}