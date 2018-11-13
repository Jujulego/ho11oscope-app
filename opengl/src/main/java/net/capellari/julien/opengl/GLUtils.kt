package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES31
import android.opengl.GLES31Ext
import android.opengl.GLES32
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object GLUtils {
    // Attributs
    const val INT_SIZE   = 4 // sizeof(int)   = 4
    const val SHORT_SIZE = 2 // sizeof(short) = 2
    const val FLOAT_SIZE = 4 // sizeof(float) = 4

    // Fonctions
    fun checkGlError(glOperation: String) {
        val error: Int = GLES31.glGetError()

        if (error != GLES31.GL_NO_ERROR) {
            Log.w("GLUtils", "$glOperation: glError $error")
        }
    }

    fun getGlShaderType(type: ShaderType): Int {
        return when (type) {
            ShaderType.FRAGMENT -> GLES31.GL_FRAGMENT_SHADER
            ShaderType.VERTEX   -> GLES31.GL_VERTEX_SHADER
            ShaderType.COMPUTE  -> GLES31.GL_COMPUTE_SHADER
            ShaderType.GEOMETRY -> GLES31Ext.GL_GEOMETRY_SHADER_EXT
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