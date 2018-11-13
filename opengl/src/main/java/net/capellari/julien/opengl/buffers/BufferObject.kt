package net.capellari.julien.opengl.buffers

import android.opengl.GLES31
import net.capellari.julien.opengl.BaseStructure
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.lang.RuntimeException
import java.nio.*

abstract class BufferObject(protected val target: Int) {
    // Attributs
    protected var buffer: ByteBuffer? = null
        private set

    val realSize: Int get() = buffer?.capacity() ?: 0
    var size: Int = 0
        private set

    var position: Int
        get()  = buffer?.position() ?: 0
        set(p) {
            buffer?.position(p)
        }

    // Méthodes
    fun allocate(size: Int) {
        // Set position and size
        position = 0
        this.size = size

        // Gardien
        if (size <= realSize) {
            return
        }

        // Allocation
        val nbuf = ByteBuffer.allocateDirect(size)
        nbuf.order(ByteOrder.nativeOrder())

        // Copy existing data
        buffer?.also {
            nbuf.position(0)
            nbuf.put(it)
            nbuf.position(0)
        }

        // Replace buffer
        buffer = nbuf
    }

    open fun bind(id: Int, usage: Int = GLES31.GL_STATIC_DRAW) {
        buffer?.also {
            position = 0
            GLES31.glBindBuffer(target, id)
            GLES31.glBufferData(target, size, it, usage)
            GLES31.glBindBuffer(target, 0)
        }
    }

    // Add values
    fun put(buffer: ShortBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }
    fun put(buffer: IntBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }
    fun put(buffer: FloatBuffer) {
        buffer.position(0)

        while (buffer.hasRemaining()) {
            put(buffer.get())
        }
    }

    fun put(array: ShortArray) = array.forEach { put(it) }
    fun put(array: IntArray)   = array.forEach { put(it) }
    fun put(array: FloatArray) = array.forEach { put(it) }

    fun put(value: Any) {
        when(value) {
            is Short -> buffer?.putShort(value)
            is Int   -> buffer?.putInt(value)
            is Float -> buffer?.putFloat(value)

            is BaseVec<*>   -> put(value.data)
            is BaseMat<*,*> -> put(value.data)
            is BaseStructure       -> value.toBuffer(this)

            else -> throw RuntimeException("Unsupported type ${value.javaClass.canonicalName}")
        }
    }

    inline fun<reified T : Any> put(array: Array<T>)      = array.forEach { put(it) }
    inline fun<reified T : Any> put(array: Collection<T>) = array.forEach { put(it) }
}