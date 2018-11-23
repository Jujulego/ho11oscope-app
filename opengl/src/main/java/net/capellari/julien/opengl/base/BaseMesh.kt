package net.capellari.julien.opengl.base

import android.opengl.GLES31
import net.capellari.julien.opengl.GLUtils
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.buffers.ElementBufferObject
import net.capellari.julien.opengl.buffers.VertexBufferObject

abstract class BaseMesh(var hasIndices: Boolean = true,
                        var hasNormals: Boolean = false) {
    // Attributs
    private var ibo = ElementBufferObject()
    private var iboId = GLES31.GL_INVALID_INDEX
    private var indiceBT: Int = GLES31.GL_UNSIGNED_INT
    @Volatile private var reloadIbo = false

    private var vbo = VertexBufferObject()
    private var vboId = GLES31.GL_INVALID_INDEX
    @Volatile private var reloadVbo = false

    private var vaoId = GLES31.GL_INVALID_INDEX
    private var vaoBound = false

    var vertexCount: Int = 0
        private set

    var vertexSize: Int = 0
        private set

    var normalSize: Int = 0
        private set

    var vertexType: Int = GLES31.GL_FLOAT
        private set

    var normalType: Int = GLES31.GL_FLOAT
        private set

    // Méthodes à redéfinir
    abstract fun getMaterial() : Material
    abstract fun getVertices() : Any
    open fun getNormals() : Any = arrayOf<Vec3>()
    open fun getIndices() : Any = ShortArray(0)

    fun draw(mode: Int) {
        bindVAO {
            if (hasIndices) {
                GLES31.glDrawElements(mode, ibo.size, indiceBT, 0)
                GLUtils.checkGlError("glDrawElements")
            } else {
                GLES31.glDrawArrays(mode, 0, vertexCount)
                GLUtils.checkGlError("glDrawArrays")
            }
        }
    }

    fun bindVAO(lambda: () -> Unit) {
        var wasBounded: Boolean? = null

        try {
            synchronized(this) {
                wasBounded = vaoBound

                if (!vaoBound) {
                    GLES31.glBindVertexArray(vaoId)
                    vaoBound = true
                }
            }

            lambda()

        } finally {
            if (wasBounded == false) {
                synchronized(this) {
                    GLES31.glBindVertexArray(0)
                    vaoBound = false
                }
            }
        }
    }

    fun reloadIndices()  { reloadIbo = true }
    fun reloadVertices() { reloadVbo = true }

    // Méthodes internes
    internal fun genBuffers() {
        // Vertex buffer
        vaoId = IntArray(1).also { GLES31.glGenVertexArrays(1, it, 0) }[0]
        vboId = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
        reloadVbo = true

        if (hasIndices) {
            iboId = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
            reloadIbo = true
        }
    }
    internal fun loadBuffers() {
        // load IBO
        if (hasIndices && reloadIbo && iboId != GLES31.GL_INVALID_INDEX) {
            val indices = getIndices()
            indiceBT = GLUtils.bufferType(indices, true)

            ibo.allocate(GLUtils.bufferSize(indices))
            ibo.put(indices)

            reloadIbo = false
        }

        // load VBO
        if (reloadVbo && vboId != GLES31.GL_INVALID_INDEX) {
            val vertices = getVertices()
            vertexCount = GLUtils.vertexCount(vertices)
            vertexSize = GLUtils.bufferSize(vertices)
            vertexType = GLUtils.bufferType(vertices)
            normalSize = 0

            if (hasNormals) {
                val normals = getNormals()
                normalSize = GLUtils.bufferSize(normals)
                normalType = GLUtils.bufferType(normals)

                vbo.allocate(vertexSize + normalSize)

                vbo.put(vertices)
                vbo.put(normals)
            } else {
                vbo.allocate(vertexSize)
                vbo.put(vertices)
            }

            reloadVbo = false
        }
    }
    internal fun bindBuffers() {
        bindVAO {
            vbo.bind(vboId)

            if (hasIndices) {
                ibo.bind(iboId)
            }
        }
    }
}