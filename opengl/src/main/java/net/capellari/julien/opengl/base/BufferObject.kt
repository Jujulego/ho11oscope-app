package net.capellari.julien.opengl.base

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    fun bind(id: Int, usage: Int = GLES20.GL_STATIC_DRAW) {
        buffer?.also {
            position = 0
            GLES20.glBindBuffer(target, id)
            GLES20.glBufferData(target, size, it, usage)
        }
    }
}