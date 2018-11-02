package net.capellari.julien.opengl

import android.opengl.GLES20
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.lang.RuntimeException
import java.nio.*
import kotlin.reflect.KClass

class VertexBufferObject {
    // Attributs
    private var vbo: ByteBuffer? = null

    val realSize: Int get() = vbo?.capacity() ?: 0
    var size: Int = 0
        private set

    var position: Int
        get()  = vbo?.position() ?: 0
        set(p) {
            vbo?.position(p)
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
        vbo?.also {
            nbuf.position(0)
            nbuf.put(it)
            nbuf.position(0)
        }

        // Replace buffer
        vbo = nbuf
    }

    fun bind(id: Int, usage: Int = GLES20.GL_STATIC_DRAW) {
        vbo?.also {
            position = 0
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, id)
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size, it, usage)
        }
    }

    // Put
    // - bases
    fun put(value: Short) {
        vbo?.putShort(value)
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
        vbo?.putInt(value)
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
        vbo?.putFloat(value)
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