package net.capellari.julien.opengl.buffers

import android.opengl.GLES31
import java.lang.RuntimeException
import java.nio.*

abstract class BufferObject(protected val target: Int) : BO {
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
    override fun getByteBuffer(): ByteBuffer? = buffer
    override fun put(value: Any): Unit = throw RuntimeException("Unsupported type ${value.javaClass.canonicalName}")
}