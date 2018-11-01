package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.nio.Buffer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class BaseProgram {
    // Companion
    companion object {
        // Attributs
        const val TAG = "BaseProgram"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        fun <T : BaseProgram> getImplementation(cls: KClass<T>) : T {
            val impl = Class.forName("${cls.qualifiedName}_Impl").kotlin.createInstance()
            return impl as T
        }
    }

    // Attributs
    private var program: Int = -1
    protected var iboId: Int = -1
    protected var vboId: Int = -1

    // Méthodes abstraites
    // - loading shaders
    protected abstract fun loadShaders(context: Context, program: Int)
    protected abstract fun getLocations()

    // - charge vars
    protected abstract fun loadUniforms()
    protected abstract fun loadIBO()
    protected abstract fun loadVBO()

    // - clean
    protected abstract fun clean()

    // Méthodes
    fun compile(context: Context) {
        // Compile program
        program = GLES20.glCreateProgram()
        loadShaders(context, program)

        // Link program
        GLES20.glLinkProgram(program)

        GLUtils.checkGlError("Linking program")
        Log.d(TAG, "Program linked")

        // Get locations
        getLocations()
        Log.d(TAG, "All locations gathered")
    }

    fun render(numIndices: Int) {
        GLES20.glUseProgram(program)

        // Load variables and buffers
        loadUniforms()

        loadVBO()
        loadIBO()

        // Bind VBO
        if (vboId != -1) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
            GLUtils.checkGlError("Binding VBO")
        }

        // Bind IBO
        if (iboId != -1) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId)
            GLUtils.checkGlError("Binding IBO")
        }

        // Draw TODO: get indice type from IBO annotation
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_INT, 0)

        // Clean up
        clean()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glUseProgram(0)
    }

    // - shaders
    protected fun loadShader(script: String, type: ShaderType, file: String? = null): Int {
        // Create shader
        val shader = GLES20.glCreateShader(GLUtils.getGlShaderType(type))
        GLES20.glShaderSource(shader, script)

        // Compilation
        GLES20.glCompileShader(shader)
        GLUtils.checkGlError("Compiling shader ${file ?: "<input>"}")

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)

        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, file?.let { "Shader compile error ! (in $it)" } ?: "Shader compile error !")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
        } else {
            Log.d(TAG, "${file ?: "Shader"} compiled")
        }

        return shader
    }
    protected fun loadShaderAsset(context: Context, file: String, type: ShaderType) : Int {
        val script = GLUtils.readFile(context, file)
        return loadShader(script, type, file)
    }

    // - locations
    protected fun getAttribLocation(name: String): Int {
        val handle = GLES20.glGetAttribLocation(program, name)

        // Print
        if (handle < 0) {
            Log.w(TAG, "Attribute $name not found")
        } else {
            Log.d(TAG, "Attribute $name : $handle")
        }

        return handle
    }
    protected fun getUniformLocation(name: String): Int {
        val handle = GLES20.glGetUniformLocation(program, name)

        // Print
        if (handle < 0) {
            Log.w(TAG, "Uniform $name not found")
        } else {
            Log.d(TAG, "Uniform $name : $handle")
        }

        return handle
    }

    // - valeurs
    protected fun setUniformValue(handle: Int, v: Any?) {
        if (handle >= 0 && v != null) {
            when (v) {
                is Float -> GLES20.glUniform1f(handle, v)
                is Vec2  -> GLES20.glUniform2f(handle, v.x, v.y)
                is Vec3  -> GLES20.glUniform3f(handle, v.x, v.y, v.z)
                is Vec4  -> GLES20.glUniform4f(handle, v.x, v.y, v.z, v.a)
                is Mat2  -> GLES20.glUniformMatrix2fv(handle, 1, false, v.data, 0)
                is Mat3  -> GLES20.glUniformMatrix3fv(handle, 1, false, v.data, 0)
                is Mat4  -> GLES20.glUniformMatrix4fv(handle, 1, false, v.data, 0)

                else -> throw RuntimeException("Unsupported value type : ${v::class.qualifiedName}")
            }
        }
    }
}