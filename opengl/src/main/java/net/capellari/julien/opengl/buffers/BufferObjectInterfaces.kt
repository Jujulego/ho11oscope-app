package net.capellari.julien.opengl.buffers

import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

interface BO {
    fun getByteBuffer() : ByteBuffer?
    fun put(value: Any)
}

interface ShortBO : BO {
    fun put(value: Short) {
        getByteBuffer()?.putShort(value)
    }
    fun put(buffer: ShortBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(array: ShortArray)   = array.forEach { put(it) }
    fun put(array: Array<Short>) = array.forEach { put(it) }
}
interface IntBO : BO {
    fun put(value: Int) {
        getByteBuffer()?.putInt(value)
    }
    fun put(buffer: IntBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(array: IntArray)   = array.forEach { put(it) }
    fun put(array: Array<Int>) = array.forEach { put(it) }
}
interface FloatBO : BO {
    fun put(value: Float) {
        getByteBuffer()?.putFloat(value)
    }
    fun put(buffer: FloatBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(array: FloatArray)   = array.forEach { put(it) }
    fun put(array: Array<Float>) = array.forEach { put(it) }

    fun put(vec: BaseVec<*>)   = put(vec.data)
    fun put(vec: BaseMat<*, *>) = put(vec.data)

    fun put(array: Array<BaseVec<*>>)   = array.forEach { put(it) }
    fun put(array: Array<BaseMat<*, *>>) = array.forEach { put(it) }
}