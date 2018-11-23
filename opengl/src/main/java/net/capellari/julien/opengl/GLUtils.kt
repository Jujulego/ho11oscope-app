package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES31
import android.opengl.GLES31Ext
import android.opengl.GLES32
import android.util.Log
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

object GLUtils {
    // Attributs
    const val INT_SIZE   = 4 // sizeof(int)   = 4
    const val SHORT_SIZE = 2 // sizeof(short) = 2
    const val FLOAT_SIZE = 4 // sizeof(float) = 4

    // Fonctions
    fun bufferSize(v: Any) : Int {
        return when (v) {
            // Base
            is Short -> GLUtils.SHORT_SIZE
            is Int   -> GLUtils.INT_SIZE
            is Float -> GLUtils.FLOAT_SIZE

            // Composed
            is BaseVec<*>    -> v.size * GLUtils.FLOAT_SIZE
            is BaseMat<*, *> -> v.size * v.size * GLUtils.FLOAT_SIZE
            is BaseStructure -> v.getBufferSize()

            // Buffers
            is ShortBuffer -> (v as Buffer).capacity() * GLUtils.SHORT_SIZE
            is IntBuffer   -> (v as Buffer).capacity() * GLUtils.INT_SIZE
            is FloatBuffer -> (v as Buffer).capacity() * GLUtils.FLOAT_SIZE

            // Arrays
            is ShortArray -> v.size * GLUtils.SHORT_SIZE
            is IntArray   -> v.size * GLUtils.INT_SIZE
            is FloatArray -> v.size * GLUtils.FLOAT_SIZE

            is Array<*>      -> if (v.isEmpty()) 0 else v.size * bufferSize(v.first()!!)
            is Collection<*> -> if (v.isEmpty()) 0 else v.size * bufferSize(v.first()!!)

            else -> throw java.lang.RuntimeException("Unsupported type ${v.javaClass.canonicalName}")
        }
    }
    fun bufferType(v: Any, unsigned: Boolean = false): Int {
        return when (v) {
            // Base
            is Short -> if (unsigned) GLES31.GL_UNSIGNED_SHORT else GLES31.GL_SHORT
            is Int   -> if (unsigned) GLES31.GL_UNSIGNED_INT   else GLES31.GL_INT
            is Float -> GLES31.GL_FLOAT

            // Composed
            is BaseVec<*>    -> GLES31.GL_FLOAT
            is BaseMat<*,*>  -> GLES31.GL_FLOAT
            is BaseStructure -> GLES31.GL_BYTE

            // Buffers
            is ShortBuffer -> if (unsigned) GLES31.GL_UNSIGNED_SHORT else GLES31.GL_SHORT
            is IntBuffer   -> if (unsigned) GLES31.GL_UNSIGNED_INT   else GLES31.GL_INT
            is FloatBuffer -> GLES31.GL_FLOAT

            // Arrays
            is ShortArray -> if (unsigned) GLES31.GL_UNSIGNED_SHORT else GLES31.GL_SHORT
            is IntArray   -> if (unsigned) GLES31.GL_UNSIGNED_INT   else GLES31.GL_INT
            is FloatArray -> GLES31.GL_FLOAT

            is Array<*>      -> if (v.isEmpty()) -1 else bufferType(v.first()!!, unsigned)
            is Collection<*> -> if (v.isEmpty()) -1 else bufferType(v.first()!!, unsigned)

            else -> throw java.lang.RuntimeException("Unsupported type ${v.javaClass.canonicalName}")
        }
    }

    fun vertexCount(c: Any) : Int {
        return when(c) {
            // Get Size
            is Collection<*> -> c.size
            is Array<*>      -> c.size
            is Buffer        -> c.capacity()

            is ShortArray -> c.size
            is IntArray   -> c.size
            is FloatArray -> c.size

            else -> 1
        }
    }

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