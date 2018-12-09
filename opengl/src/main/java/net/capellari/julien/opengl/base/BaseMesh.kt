package net.capellari.julien.opengl.base

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES31
import android.util.Log
import net.capellari.julien.opengl.GLUtils
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.Vec2
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.buffers.ElementBufferObject
import net.capellari.julien.opengl.buffers.VertexBufferObject
import java.io.File

abstract class BaseMesh(var hasIndices: Boolean = true,
                        var hasNormals: Boolean = false,
                        var hasTexCoords: Boolean = false) {
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

    var texCoordSize: Int = 0
        private set

    var texCoordType: Int = GLES31.GL_FLOAT
        private set

    val othersOff = mutableMapOf<String,Int>()
    val othersType = mutableMapOf<String,Int>()

    // Méthodes à redéfinir
    open     fun getMaterial()  = Material("")
    abstract fun getVertices()  : Any
    open     fun getNormals()   : Any = arrayOf<Vec3>()
    open     fun getTexCoords() : Any = arrayOf<Vec2>()
    open     fun getIndices()   : Any = ShortArray(0)
    open fun getOther(name: String) : Any = ShortArray(0)

    fun draw(mode: Int) {
        // Bind texture
        getMaterial().bindTexture()

        // Draw !
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

        // Indices Buffer
        if (hasIndices) {
            ibo.generate()
        }
    }
    internal fun loadTexture(context: Context) {
        getMaterial().loadTexture(context)
    }
    internal fun loadBuffers(others: Array<String>) {
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
            var size = 0

            // Vertices data
            val vertices = getVertices()
            vertexCount = GLUtils.vertexCount(vertices)
            vertexSize = GLUtils.bufferSize(vertices)
            vertexType = GLUtils.bufferType(vertices)

            size += vertexSize

            // Normals data
            normalSize = 0
            var normals: Any? = null

            if (hasNormals) {
                normals = getNormals()
                normalSize = GLUtils.bufferSize(normals)
                normalType = GLUtils.bufferType(normals)

                size += normalSize
            }

            // TexCoord data
            texCoordSize = 0
            var texCoords: Any? = null

            if (hasTexCoords) {
                texCoords = getTexCoords()
                texCoordSize = GLUtils.bufferSize(texCoords)
                texCoordType = GLUtils.bufferType(texCoords)

                size += texCoordSize
            }

            // Others
            val othersV = mutableListOf<Any>()

            for (attr in others) {
                val v = getOther(attr)
                val s = GLUtils.bufferSize(v)

                othersV.add(v)
                othersOff[attr] = size
                othersType[attr] = GLUtils.bufferType(v)

                size += s
            }

            // Fill buffer
            vbo.allocate(size)
            vbo.put(vertices)
            normals?.also { vbo.put(it) }
            texCoords?.also { vbo.put(it) }
            othersV.forEach { vbo.put(it) }

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