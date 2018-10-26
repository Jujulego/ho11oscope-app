package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object GLUtils {
    // Attributs
    private val TAG = "PolySample"
    val FLOAT_SIZE = 4  // sizeof(float) is 4 bytes.
    val SHORT_SIZE = 2  // sizeof(short) is 2 bytes.

    val COORDS_PER_VERTEX = 3
    val NUM_COLOR_COMPONENTS = 4 // r, g, b, a

    // Méthodes
    fun checkGlError(glOperation: String) {
        val error: Int = GLES20.glGetError()

        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("$glOperation: glError $error")
        }
    }

    fun loadShader(type: Int, file: String, context: Context): Int {
        // read file
        val shaderCode = StringBuffer()
        val reader = BufferedReader(InputStreamReader(context.assets.open(file)))

        do {
            val s: String? = reader.readLine()
            shaderCode.append(s)
        } while (s != null)

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode.toString())
        GLES20.glCompileShader(shader)
        GLUtils.checkGlError("compile shader")

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)

        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Shader compile error!")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
        }

        return shader
    }

    fun createVbo(data: FloatBuffer): Int {
        val vbos = IntArray(1)
        data.position(0)

        GLES20.glGenBuffers(1, vbos, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.capacity() * GLUtils.FLOAT_SIZE, data,
                GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        return vbos[0]
    }

    fun createIbo(data: ShortBuffer): Int {
        val ibos = IntArray(1)
        data.position(0)

        GLES20.glGenBuffers(1, ibos, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibos[0])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, data.capacity() * GLUtils.SHORT_SIZE,
                data, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        return ibos[0]
    }
}