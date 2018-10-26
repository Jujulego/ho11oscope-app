package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLES20

class Shaders(context: Context) {
    // Attributs
    private var program: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0

    // Constructeur
    init {
        init(context)
    }

    // Méthodes
    private fun init(context: Context) {
        // load shaders
        val vertexShader = GLUtils.loadShader(GLES20.GL_VERTEX_SHADER, "shaders/vertex.glsl", context)
        val fragmentShader = GLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, "shaders/fragment.glsl", context)

        // compile and link
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        GLUtils.checkGlError("link program")

        // get handles
        GLES20.glUseProgram(program)
        positionHandle  = GLES20.glGetAttribLocation(program, "aPosition")
        colorHandle     = GLES20.glGetAttribLocation(program, "aColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUseProgram(0)

        GLUtils.checkGlError("get handles")
    }

    fun render(mvpMatrix: FloatArray, numIndices: Int, ibo: Int, positionVbo: Int, colorsVbo: Int) {
        GLES20.glUseProgram(program)

        // feed positions to shader from position VBO
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionVbo)
        GLES20.glVertexAttribPointer(positionHandle, GLUtils.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, 0)

        // feed colors to shader from color VBO
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorsVbo)
        GLES20.glVertexAttribPointer(colorHandle, GLUtils.NUM_COLOR_COMPONENTS, GLES20.GL_FLOAT, false, 0, 0)

        // feed MVP Matrix to shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Bind IBO and render
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, 0)

        GLUtils.checkGlError("render")

        // Clean up
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glUseProgram(0)
    }
}