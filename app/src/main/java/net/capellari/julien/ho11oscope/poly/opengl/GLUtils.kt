package net.capellari.julien.ho11oscope.poly.opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.*

object GLUtils {
    // Attributs
    private val TAG = "PolySample"
    const val FLOAT_SIZE = 4  // sizeof(float) is 4 bytes.
    const val INT_SIZE   = 4  // sizeof(int)   is 4 bytes.
    const val SHORT_SIZE = 2  // sizeof(short) is 2 bytes.

    const val COORDS_PER_VERTEX = 3
    const val NUM_COLOR_COMPONENTS = 3 // r, g, b

    // Classes
    data class VBO(val id: Int, val vSize: Int, val nSize: Int, val caSize: Int, val cdSize: Int, val csSize: Int, val seSize: Int, val oSize: Int) {
        // Propriétés
        val vOffset: Int = 0                // vertices
        val nOffset: Int = vOffset+vSize    // normals
        val caOffset: Int = nOffset+nSize   // ambient color
        val cdOffset: Int = caOffset+caSize // diffuse color
        val csOffset: Int = cdOffset+cdSize // specular color
        val seOffset: Int = csOffset+csSize // specular exponent
        val oOffset:  Int = seOffset+seSize // specular exponent

        // Constructeurs
        constructor() : this(0, 0, 0, 0, 0, 0, 0, 0)
    }

    // Méthodes
    fun checkGlError(glOperation: String) {
        val error: Int = GLES20.glGetError()

        if (error != GLES20.GL_NO_ERROR) {
            Log.w("GLUtils", "$glOperation: glError $error")
        }
    }

    fun loadShader(type: Int, file: String, context: Context): Int {
        // read file
        val shaderCode = StringBuffer()
        val reader = BufferedReader(InputStreamReader(context.assets.open(file)))

        do {
            val s: String? = reader.readLine()
            shaderCode.append("\n").append(s)
        } while (s != null)

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode.toString())
        GLES20.glCompileShader(shader)
        checkGlError("compile shader $file")

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)

        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Shader compile error ! ($file)")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
        }

        Log.d("Shaders", "$file compiled")

        return shader
    }

    fun createVbo(vertices: FloatBuffer, normals: FloatBuffer, ambientColors: FloatBuffer, diffuseColors: FloatBuffer, specularColors: FloatBuffer, specularExps: FloatBuffer, opacities: FloatBuffer): VBO {
        // Init
        val vboId = IntArray(1)
        val vSize = vertices.capacity() * FLOAT_SIZE
        val nSize = normals.capacity() * FLOAT_SIZE
        val caSize = ambientColors.capacity() * FLOAT_SIZE
        val cdSize = diffuseColors.capacity() * FLOAT_SIZE
        val csSize = specularColors.capacity() * FLOAT_SIZE
        val seSize = specularExps.capacity() * FLOAT_SIZE
        val oSize = opacities.capacity() * FLOAT_SIZE

        Log.d("GLUtils", "creating VBO: ${vertices.capacity() / COORDS_PER_VERTEX} vertices" +
                ", ${normals.capacity() / COORDS_PER_VERTEX} normals" +
                ", ${ambientColors.capacity() / NUM_COLOR_COMPONENTS} ambientColors" +
                ", ${diffuseColors.capacity() / NUM_COLOR_COMPONENTS} diffuseColors" +
                ", ${specularColors.capacity() / NUM_COLOR_COMPONENTS} specularColors" +
                ", ${specularExps.capacity() / NUM_COLOR_COMPONENTS} specularExps" +
                ", ${opacities.capacity() / NUM_COLOR_COMPONENTS} opacities")

        // Create big buffer
        vertices.position(0)
        normals.position(0)
        ambientColors.position(0)
        diffuseColors.position(0)
        specularColors.position(0)
        specularExps.position(0)
        opacities.position(0)

        val data = ByteBuffer.allocateDirect(vSize + nSize + caSize + cdSize + csSize + seSize + oSize)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        // Concat buffers
        data.position(0)
        data.put(vertices).put(normals).put(ambientColors).put(diffuseColors).put(specularColors).put(specularExps).put(opacities)
        data.position(0)

        Log.d("GLUtils", "VBO size: ${data.capacity()}")

        // Create VBO
        GLES20.glGenBuffers(1, vboId, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vSize + nSize + caSize + cdSize + csSize + seSize + oSize, data, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        return VBO(vboId[0], vSize, nSize, caSize, cdSize, csSize, seSize, oSize)
    }

    fun createIbo(data: IntBuffer): Int {
        val iboId = IntArray(1)
        data.position(0)

        Log.d("GLUtils", "creating IBO: ${data.capacity()} indexes")

        // Create IBO
        GLES20.glGenBuffers(1, iboId, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId[0])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, data.capacity() * INT_SIZE, data, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        return iboId[0]
    }
}