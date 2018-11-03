package net.capellari.julien.opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.nio.*
import kotlin.reflect.KClass
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

    protected var ibo = IndiceBufferObject()
    protected var iboId: Int = -1

    protected var vbo = VertexBufferObject()
    protected var vboId: Int = -1
    protected var reloadVBO  = false

    // Config
    var mode: Int = GLES20.GL_TRIANGLES

    // Méthodes abstraites
    // - loading shaders
    protected abstract fun loadShaders(context: Context, program: Int)
    protected abstract fun getLocations()

    // - charge vars
    protected abstract fun loadUniforms()
    protected abstract fun loadIBO()
    protected abstract fun loadVBO()
    protected abstract fun enableVBO()

    // - draw & clean
    protected abstract fun draw()
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

        // Initialisation
        usingProgram {
            // Load variables
            loadUniforms()

            // Création des buffers
            iboId = IntArray(1).also { GLES20.glGenBuffers(1, it, 0) }[0]
            vboId = IntArray(1).also { GLES20.glGenBuffers(1, it, 0) }[0]
            reloadVBO = true
        }

        loadIBO()
    }
    fun render() {
        usingProgram {
            // Load buffers
            if (reloadVBO) {
                loadVBO()
                reloadVBO = false
            }

            // Bind VBO
            if (vboId != -1) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
                enableVBO()

                GLUtils.checkGlError("Binding VBO")
            }

            // Bind IBO
            if (iboId != -1) {
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId)
                GLUtils.checkGlError("Binding IBO")
            }

            // Draw
            draw()

            // Clean up
            clean()
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    // - contexte
    fun usingProgram(lambda: () -> Unit) {
        var wasActive: Boolean? = null

        try {
            // Activate program
            synchronized(this) {
                wasActive = isActive
                GLES20.glUseProgram(program)
                isActive = true
            }

            return lambda()

        } finally {
            // Disactivate program
            wasActive?.also {
                synchronized(this) {
                    if (it) GLES20.glUseProgram(0)
                    isActive = it
                }
            }
        }
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
            is BaseVec<*>   -> (v as BaseVec<*>).size * GLUtils.FLOAT_SIZE
            is BaseMat<*,*> -> (v as BaseMat<*,*>).run { size * size * GLUtils.FLOAT_SIZE }

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
            is Short -> if (unsigned) GLES20.GL_UNSIGNED_SHORT else GLES20.GL_SHORT
            is Int   -> if (unsigned) GLES20.GL_UNSIGNED_INT   else GLES20.GL_INT
            is Float -> GLES20.GL_FLOAT

            // Composed
            is BaseVec<*>   -> GLES20.GL_FLOAT
            is BaseMat<*,*> -> GLES20.GL_FLOAT

            // Buffers
            is ShortBuffer -> if (unsigned) GLES20.GL_UNSIGNED_SHORT else GLES20.GL_SHORT
            is IntBuffer   -> if (unsigned) GLES20.GL_UNSIGNED_INT   else GLES20.GL_INT
            is FloatBuffer -> GLES20.GL_FLOAT

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
            is BaseVec<*>   -> (v as BaseVec<*>).size
            is BaseMat<*,*> -> (v as BaseMat<*,*>).run { size * size }

            else -> throw java.lang.RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun<T:Buffer> allocateOrReuse(size: Int, buffer: T?, bufferType: BufferType) : T? {
        // Reuse initial buffer
        if (size <= buffer?.capacity() ?: 0) return buffer

        // Allocation
        val buf = ByteBuffer.allocateDirect(size)
                .order(ByteOrder.nativeOrder())
        buf.position(0)

        return when (bufferType) {
            BufferType.SHORT -> buf.asShortBuffer() as T
            BufferType.INT   -> buf.asIntBuffer()   as T
            BufferType.FLOAT -> buf.asFloatBuffer() as T
        }
    }

    // - valeurs
    protected fun setUniformValue(handle: Int, v: Any?) {
        if (handle >= 0 && v != null) {
            when (v) {
                is Int   -> GLES20.glUniform1i(handle, v)
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