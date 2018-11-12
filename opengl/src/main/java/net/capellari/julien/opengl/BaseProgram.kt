package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES31
import android.util.Log
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import net.capellari.julien.opengl.buffers.ElementBufferObject
import net.capellari.julien.opengl.buffers.VertexBufferObject
import java.nio.*
import kotlin.reflect.full.createInstance

abstract class BaseProgram {
    // Companion
    companion object {
        // Attributs
        const val TAG = "BaseProgram"

        // Méthodes
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : BaseProgram> getImplementation() : T =
                Class.forName("${T::class.qualifiedName}_Impl").kotlin.createInstance() as T
    }

    // Attributs
    private var program: Int = -1
    private var isActive = false
    protected var reloadUniforms = false

    protected var ibo = ElementBufferObject()
    protected var iboId: Int = -1
    protected var reloadIBO  = false

    protected var vbo = VertexBufferObject()
    protected var vboId: Int = -1
    protected var reloadVBO  = false

    // Config
    var mode: Int = GLES31.GL_TRIANGLES
    var defaultMode: Int = GLES31.GL_TRIANGLES
        private set

    // Méthodes abstraites
    // - loading shaders
    protected abstract fun loadShaders(context: Context, program: Int)
    protected abstract fun getLocations()
    protected abstract fun genBuffers()

    // - charge vars
    protected abstract fun loadUniforms()
    protected abstract fun loadBuffers()
    protected abstract fun loadIBO()
    protected abstract fun loadVBO()
    protected abstract fun enableVBO()

    // - draw & clean
    protected abstract fun draw()
    protected abstract fun clean()

    // Méthodes
    fun compile(context: Context) {
        // Compile program
        program = GLES31.glCreateProgram()
        loadShaders(context, program)

        // Link program
        GLES31.glLinkProgram(program)

        GLUtils.checkGlError("Linking program")
        Log.d(TAG, "Program linked")

        // Get locations
        getLocations()
        Log.d(TAG, "All locations gathered")

        // Initialisation
        usingProgram {
            // Création des buffers
            iboId = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
            vboId = IntArray(1).also { GLES31.glGenBuffers(1, it, 0) }[0]
            genBuffers()
        }

        // First load
        reloadUniforms = true
        reloadVBO = true
        reloadIBO = true
    }
    fun render() {
        usingProgram {
            // Load variables
            if (reloadUniforms) {
                loadUniforms()
                reloadUniforms = false
            }

            loadBuffers()

            // Load buffers
            if (reloadVBO) {
                loadVBO()
                reloadVBO = false
            }

            if (reloadIBO) {
                loadIBO()
                reloadIBO = false
            }

            // Bind VBO
            if (vboId != -1) {
                GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vboId)
                enableVBO()

                GLUtils.checkGlError("Binding VBO")
            }

            // Bind IBO
            if (iboId != -1) {
                GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, iboId)
                GLUtils.checkGlError("Binding IBO")
            }

            // Draw
            draw()

            // Clean up
            clean()
            GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, 0)
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0)
        }
    }

    // - contexte
    fun usingProgram(lambda: () -> Unit) {
        var wasActive: Boolean? = null

        try {
            // Activate program
            synchronized(this) {
                wasActive = isActive
                GLES31.glUseProgram(program)
                isActive = true
            }

            return lambda()

        } finally {
            // Disactivate program
            wasActive?.also {
                synchronized(this) {
                    if (it) GLES31.glUseProgram(0)
                    isActive = it
                }
            }
        }
    }

    // - shaders
    protected fun loadShader(script: String, type: ShaderType, file: String? = null): Int {
        // Create shader
        val shader = GLES31.glCreateShader(GLUtils.getGlShaderType(type))
        GLES31.glShaderSource(shader, script)

        // Compilation
        GLES31.glCompileShader(shader)
        GLUtils.checkGlError("Compiling shader ${file ?: "<input>"}")

        val status = IntArray(1)
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, status, 0)

        if (status[0] != GLES31.GL_TRUE) {
            Log.e(TAG, file?.let { "Shader compile error ! (in $it)" } ?: "Shader compile error !")
            Log.e(TAG, GLES31.glGetShaderInfoLog(shader))
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
        val handle = GLES31.glGetAttribLocation(program, name)

        // Print
        if (handle == GLES31.GL_INVALID_INDEX) {
            Log.w(TAG, "Attribute $name not found")
        } else {
            Log.d(TAG, "Attribute $name : $handle")
        }

        return handle
    }
    protected fun getUniformLocation(name: String): Int {
        val handle = GLES31.glGetUniformLocation(program, name)

        // Print
        if (handle == GLES31.GL_INVALID_INDEX) {
            Log.w(TAG, "Uniform $name not found")
        } else {
            Log.d(TAG, "Uniform $name : $handle")
        }

        return handle
    }
    protected fun bindUniformBlock(name: String, binding: Int) : Boolean {
        val index = GLES31.glGetUniformBlockIndex(program, name)

        // Print
        if (index == GLES31.GL_INVALID_INDEX) {
            Log.w(TAG, "UniformBlock $name not found")
            return false
        }

        Log.d(TAG, "UniformBlock $name : $index")
        GLES31.glUniformBlockBinding(program, index, binding)
        GLUtils.checkGlError("Getting ShaderStorage $name")
        return true
    }
    protected fun bindSharedStorage(name: String) : Int {
        val index = GLES31.glGetProgramResourceIndex(program, GLES31.GL_SHADER_STORAGE_BLOCK, name)

        // Print
        if (index == GLES31.GL_INVALID_INDEX) {
            Log.w(TAG, "ShaderStorage $name not found")
            return GLES31.GL_INVALID_INDEX
        }

        val binding = IntArray(1).also {
                GLES31.glGetProgramResourceiv(program,
                        GLES31.GL_SHADER_STORAGE_BLOCK, index,
                        1, IntArray(1) {GLES31.GL_BUFFER_BINDING}, 0,
                        1, IntArray(1) {1}, 0, it, 0)
            }[0]

        Log.d(TAG, "ShaderStorage $name : $index => $binding")
        GLUtils.checkGlError("Getting ShaderStorage $name")

        return binding
    }

    // - buffers
    protected inline fun<reified T : Any> vertexCount(c: T) : Int {
        return when(c) {
            // Get Size
            is Collection<*> -> (c as Collection<*>).size
            is Array<*>      -> (c as Array<*>).size
            is Buffer        -> (c as Buffer).capacity()

            else -> 1
        }
    }

    protected inline fun<reified T : Any> bufferSize(c: Collection<T>) : Int {
        return if (c.isEmpty()) 0 else c.size * bufferSize(c.first())
    }
    protected inline fun<reified T : Any> bufferSize(c: Array<T>) : Int {
        return if (c.isEmpty()) 0 else c.size * bufferSize(c.first())
    }
    protected inline fun<reified T : Any> bufferSize(v: T) : Int {
        return when (v) {
            // Base
            is Short -> GLUtils.SHORT_SIZE
            is Int   -> GLUtils.INT_SIZE
            is Float -> GLUtils.FLOAT_SIZE

            // Composed
            is BaseVec<*>   -> v.size * GLUtils.FLOAT_SIZE
            is BaseMat<*,*> -> v.size * v.size * GLUtils.FLOAT_SIZE
            is Struct       -> v.getBufferSize()

            // Buffers
            is ShortBuffer -> (v as Buffer).capacity() * GLUtils.SHORT_SIZE
            is IntBuffer   -> (v as Buffer).capacity() * GLUtils.INT_SIZE
            is FloatBuffer -> (v as Buffer).capacity() * GLUtils.FLOAT_SIZE

            else -> throw java.lang.RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }

    protected inline fun<reified T : Any> bufferType(c: Collection<T>, unsigned: Boolean = false) : Int {
        return if (c.isEmpty()) -1 else bufferType(c.first(), unsigned)
    }
    protected inline fun<reified T : Any> bufferType(c: Array<T>, unsigned: Boolean = false) : Int {
        return if (c.isEmpty()) -1 else bufferType(c.first(), unsigned)
    }
    protected inline fun<reified T : Any> bufferType(v: T, unsigned: Boolean = false): Int {
        return when (v) {
            // Base
            is Short -> if (unsigned) GLES31.GL_UNSIGNED_SHORT else GLES31.GL_SHORT
            is Int   -> if (unsigned) GLES31.GL_UNSIGNED_INT   else GLES31.GL_INT
            is Float -> GLES31.GL_FLOAT

            // Composed
            is BaseVec<*>   -> GLES31.GL_FLOAT
            is BaseMat<*,*> -> GLES31.GL_FLOAT
            is Struct       -> GLES31.GL_BYTE

            // Buffers
            is ShortBuffer -> if (unsigned) GLES31.GL_UNSIGNED_SHORT else GLES31.GL_SHORT
            is IntBuffer   -> if (unsigned) GLES31.GL_UNSIGNED_INT   else GLES31.GL_INT
            is FloatBuffer -> GLES31.GL_FLOAT

            else -> throw java.lang.RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }

    protected inline fun<reified T : Any> numberComponents(c: Collection<T>) : Int {
        return if (c.isEmpty()) 0 else numberComponents(c.first())
    }
    protected inline fun<reified T : Any> numberComponents(c: Array<T>) : Int {
        return if (c.isEmpty()) 0 else numberComponents(c.first())
    }
    protected inline fun<reified T : Any> numberComponents(v: T): Int {
        return when (v) {
            // Base
            is Short, is Int, is Float -> 1

            // Composed
            is BaseVec<*>   -> v.size
            is BaseMat<*,*> -> v.size * v.size
            is Struct       -> v.getBufferSize()

            else -> throw java.lang.RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }

    // - valeurs
    protected fun setUniformValue(handle: Int, v: IntArray) {
        if (handle != GLES31.GL_INVALID_INDEX) GLES31.glUniform1iv(handle, v.size, v, 0)
    }
    protected fun setUniformValue(handle: Int, v: IntBuffer) {
        if (handle != GLES31.GL_INVALID_INDEX) GLES31.glUniform1iv(handle, v.capacity(), v)
    }
    protected fun setUniformValue(handle: Int, v: Array<Int>) = setUniformValue(handle, v.toIntArray())

    protected fun setUniformValue(handle: Int, v: FloatArray) {
        if (handle != GLES31.GL_INVALID_INDEX) GLES31.glUniform1fv(handle, v.size, v, 0)
    }
    protected fun setUniformValue(handle: Int, v: FloatBuffer) {
        if (handle != GLES31.GL_INVALID_INDEX) GLES31.glUniform1fv(handle, v.capacity(), v)
    }
    protected fun setUniformValue(handle: Int, v: Array<Float>) = setUniformValue(handle, v.toFloatArray())

    protected fun<T : BaseVec<*>> setUniformValue(handle: Int, v: Array<T>) {
        if (!v.isEmpty() && handle != GLES31.GL_INVALID_INDEX) {
            val vecSize = v[0].size
            val array = FloatArray(v.size * vecSize) { i -> v[i / vecSize].data[i % vecSize] }

            setUniformValue(handle, array)
        }
    }
    protected fun<T : BaseMat<*,*>> setUniformValue(handle: Int, v: Array<T>) {
        if (!v.isEmpty() && handle != GLES31.GL_INVALID_INDEX) {
            val matSize = v[0].size * v[0].size
            val array = FloatArray(v.size * matSize) { i -> v[i / matSize].data[i % matSize] }

            setUniformValue(handle, array)
        }
    }

    protected fun setUniformValue(handle: Int, v: Any?) {
        if (handle != GLES31.GL_INVALID_INDEX && v != null) {
            when (v) {
                is Int   -> GLES31.glUniform1i(handle, v)
                is Float -> GLES31.glUniform1f(handle, v)
                is Vec2  -> GLES31.glUniform2f(handle, v.x, v.y)
                is Vec3  -> GLES31.glUniform3f(handle, v.x, v.y, v.z)
                is Vec4  -> GLES31.glUniform4f(handle, v.x, v.y, v.z, v.a)
                is Mat2  -> GLES31.glUniformMatrix2fv(handle, 1, false, v.data, 0)
                is Mat3  -> GLES31.glUniformMatrix3fv(handle, 1, false, v.data, 0)
                is Mat4  -> GLES31.glUniformMatrix4fv(handle, 1, false, v.data, 0)

                else -> throw RuntimeException("Unsupported value type : ${v::class.qualifiedName}")
            }
        }
    }
}