package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object GLUtils {
    // Attributs
    const val COORDS_PER_VERTEX    = 3 // xyz
    const val NUM_COLOR_COMPONENTS = 3 // rgb

    // - type size
    const val INT_SIZE   = 4 // sizeof(int)   = 4
    const val SHORT_SIZE = 2 // sizeof(short) = 2
    const val FLOAT_SIZE = 4 // sizeof(float) = 4

    // Fonctions
    fun checkGlError(glOperation: String) {
        val error: Int = GLES20.glGetError()

        if (error != GLES20.GL_NO_ERROR) {
            Log.w("GLUtils", "$glOperation: glError $error")
        }
    }

    fun getGlShaderType(type: ShaderType): Int {
        return when (type) {
            ShaderType.FRAGMENT -> GLES20.GL_FRAGMENT_SHADER
            ShaderType.VERTEX -> GLES20.GL_VERTEX_SHADER
        }
    }

    fun readFile(context: Context, file: String): String {
        // read file
        val shaderCode = StringBuffer()
        val reader = BufferedReader(InputStreamReader(context.assets.open(file)))

        do {
            val s: String? = reader.readLine()
            shaderCode.append("\n").append(s)
        } while (s != null)

        return shaderCode.toString()
    }
}