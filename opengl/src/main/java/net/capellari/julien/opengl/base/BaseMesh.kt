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
    private var indiceBT: Int = GLES31.GL_UNSIGNED_INT

    private var vaoId = GLES31.GL_INVALID_INDEX
    private var vaoBound = false

    private var vbo = VertexBufferObject()
    var vertexCount: Int = 0
        private set

    var vertexSize: Int = 0
        private set

    var vertexType: Int = GLES31.GL_FLOAT
        private set

    var normalSize: Int = 0
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
    fun bindVBO(lambda: () -> Unit) {
        vbo.bind(lambda)
    }

    fun reloadIndices()  { ibo.reload = true }
    fun reloadVertices() { vbo.reload = true }

    // Méthodes internes
    internal fun genBuffers() {
        // Vertex buffer
        vaoId = IntArray(1).also { GLES31.glGenVertexArrays(1, it, 0) }[0]

        vbo.generate()

        if (hasIndices) {
            ibo.generate()
        }
    }
    internal fun loadBuffers() {
        // load IBO
        if (hasIndices && ibo.reload) {
            val indices = getIndices()
            indiceBT = GLUtils.bufferType(indices, true)

            ibo.allocate(GLUtils.bufferSize(indices))
            ibo.put(indices)

            ibo.reload = false
        }

        // load VBO
        if (vbo.reload) {
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

            vbo.reload = false
        }
    }
    internal fun bindBuffers() {
        bindVAO {
            vbo.toGPU()

            if (hasIndices) {
                ibo.toGPU()
            }
        }
    }
}