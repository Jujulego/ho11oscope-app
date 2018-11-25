package net.capellari.julien.opengl.base

import android.opengl.GLES31
import java.lang.RuntimeException
import java.nio.*

abstract class BaseBufferObject(protected val target: Int) {
    // Attributs
    private var bound = false
    protected var id = GLES31.GL_INVALID_INDEX
    protected var buffer: ByteBuffer? = null
        private set

    val realSize: Int get() = buffer?.capacity() ?: 0
    var size: Int = 0
        private set

    val generated get() = (id != GLES31.GL_INVALID_INDEX)
    var position: Int
        get()  = buffer?.position() ?: 0
        set(p) {
            buffer?.position(p)
        }

    var reload = false
        get() = field && generated

    // Méthodes
    fun generate() {
        id = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
        reload = true
    }

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

    fun bind(lambda: () -> Unit) {
        var wasBounded: Boolean? = null

        try {
            synchronized(this) {
                wasBounded = bound

                if (!bound) {
                    GLES31.glBindBuffer(target, id)
                    bound = true
                }
            }

            lambda()

        } finally {
            if (wasBounded == false) {
                synchronized(this) {
                    GLES31.glBindVertexArray(0)
                    bound = false
                }
            }
        }
    }

    open fun toGPU(usage: Int = GLES31.GL_STATIC_DRAW) {
        position = 0

        bind {
            buffer?.also { GLES31.glBufferData(target, size, it, usage) }
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

    fun put(value: Any) {
        when(value) {
            is Short -> buffer?.putShort(value)
            is Int   -> buffer?.putInt(value)
            is Float -> buffer?.putFloat(value)

            is BaseVec<*>    -> put(value.data)
            is BaseMat<*,*>  -> put(value.data)
            is BaseStructure -> value.toBuffer(this)

            is ShortArray -> value.forEach { put(it) }
            is IntArray   -> value.forEach { put(it) }
            is FloatArray -> value.forEach { put(it) }

            is Array<*>      -> value.forEach { put(it!!) }
            is Collection<*> -> value.forEach { put(it!!) }

            else -> throw RuntimeException("Unsupported type ${value.javaClass.canonicalName}")
        }
    }
}