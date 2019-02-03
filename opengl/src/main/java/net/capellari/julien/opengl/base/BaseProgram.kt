package net.capellari.julien.opengl.base

import android.content.Context
import android.opengl.GLES32
import android.util.Log
import net.capellari.julien.opengl.*
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

    // Config
    var mode: Int = GLES32.GL_TRIANGLES
    var defaultMode: Int = GLES32.GL_TRIANGLES
        private set

    protected var otherAttrs = arrayOf<String>()

    // Méthodes abstraites
    // - loading shaders
    protected abstract fun loadShaders(context: Context, program: Int)
    protected abstract fun getLocations()
    protected abstract fun genBuffers()

    // - charge vars
    protected abstract fun loadUniforms()
    protected abstract fun loadBuffers()
    protected abstract fun enableVBO(mesh: Mesh)
    protected abstract fun setMeshMaterial(mesh: Mesh)

    // Méthodes
    fun compile(context: Context) {
        // Compile program
        program = GLES32.glCreateProgram()
        loadShaders(context, program)

        // Link program
        GLES32.glLinkProgram(program)

        GLUtils.checkGlError("Linking program")
        Log.d(TAG, "BaseProgram linked")

        // Initialisation
        usingProgram {
            // Get locations
            getLocations()
            Log.d(TAG, "All locations gathered")

            // Création des buffers
            genBuffers()
        }

        // First load
        reloadUniforms = true
    }

    fun prepare(mesh: Mesh) = prepare(arrayListOf(mesh))
    fun prepare(meshes: Collection<Mesh>) {
        usingProgram {
            for (mesh in meshes) {
                mesh.genBuffers()
                mesh.loadTexture()
            }
        }
    }

    fun render(mesh: Mesh) = render(arrayListOf(mesh))
    fun render(meshes: Collection<Mesh>) {
        usingProgram {
            // Load variables
            if (reloadUniforms) {
                loadUniforms()
                reloadUniforms = false
            }

            loadBuffers()

            for (mesh in meshes) {
                // Préparations des buffers
                mesh.loadBuffers(otherAttrs)
                setMeshMaterial(mesh)

                mesh.bindVAO {
                    // Draw
                    mesh.bindBuffers()
                    enableVBO(mesh)

                    mesh.draw(mode)
                }
            }
        }
    }

    // - contexte
    fun usingProgram(lambda: () -> Unit) {
        var wasActive: Boolean? = null

        try {
            // Activate program
            synchronized(this) {
                wasActive = isActive

                if (!isActive) {
                    GLES32.glUseProgram(program)
                    isActive = true
                }
            }

            return lambda()

        } finally {
            // Disactivate program
            if (wasActive == false) {
                synchronized(this) {
                    GLES32.glUseProgram(0)
                    isActive = false
                }
            }
        }
    }

    // - shaders
    protected fun loadShader(script: String, type: ShaderType, file: String? = null): Int {
        // Create shader
        val shader = GLES32.glCreateShader(GLUtils.getGlShaderType(type))
        GLES32.glShaderSource(shader, script)
        GLUtils.checkGlError("Creating shader ${file ?: "<input>"}")

        // Compilation
        GLES32.glCompileShader(shader)
        GLUtils.checkGlError("Compiling shader ${file ?: "<input>"}")

        val status = IntArray(1)
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, status, 0)

        if (status[0] != GLES32.GL_TRUE) {
            Log.e(TAG, file?.let { "Shader compile error ! (in $it)" } ?: "Shader compile error !")
            Log.e(TAG, GLES32.glGetShaderInfoLog(shader))
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
        val handle = GLES32.glGetAttribLocation(program, name)

        // Print
        if (handle == GLES32.GL_INVALID_INDEX) {
            Log.w(TAG, "Attribute $name not found")
        } else {
            Log.d(TAG, "Attribute $name : $handle")
        }

        return handle
    }
    protected fun getUniformLocation(name: String): Int {
        val handle = GLES32.glGetUniformLocation(program, name)

        // Print
        if (handle == GLES32.GL_INVALID_INDEX) {
            Log.w(TAG, "Uniform $name not found")
        //} else {
        //    Log.d(TAG, "Uniform $name : $handle")
        }

        return handle
    }
    protected fun bindUniformBlock(name: String, binding: Int) : Boolean {
        val index = GLES32.glGetUniformBlockIndex(program, name)

        // Print
        if (index == GLES32.GL_INVALID_INDEX) {
            Log.w(TAG, "BaseUniformBlock $name not found")
            return false
        }

        Log.d(TAG, "BaseUniformBlock $name : $index => $binding")
        GLES32.glUniformBlockBinding(program, index, binding)
        GLUtils.checkGlError("Getting BaseUniformBlock $name")
        return true
    }
    protected fun bindSharedStorage(name: String) : Int {
        val index = GLES32.glGetProgramResourceIndex(program, GLES32.GL_SHADER_STORAGE_BLOCK, name)

        // Print
        if (index == GLES32.GL_INVALID_INDEX) {
            Log.w(TAG, "SharedStorage $name not found")
            return GLES32.GL_INVALID_INDEX
        }

        val binding = IntArray(1).also {
                GLES32.glGetProgramResourceiv(program,
                        GLES32.GL_SHADER_STORAGE_BLOCK, index,
                        1, IntArray(1) { GLES32.GL_BUFFER_BINDING }, 0,
                        1, IntArray(1) { 1 }, 0, it, 0)
            }[0]

        Log.d(TAG, "SharedStorage $name : $index => $binding")
        GLUtils.checkGlError("Getting SharedStorage $name")

        return binding
    }

    // - buffers
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
            is Structure -> v.getBufferSize()

            else -> throw java.lang.RuntimeException("Unsupported type ${T::class.qualifiedName}")
        }
    }

    // - valeurs
    protected fun setUniformValue(handle: Int, v: IntArray) {
        if (handle != GLES32.GL_INVALID_INDEX) GLES32.glUniform1iv(handle, v.size, v, 0)
    }
    protected fun setUniformValue(handle: Int, v: IntBuffer) {
        if (handle != GLES32.GL_INVALID_INDEX) GLES32.glUniform1iv(handle, v.capacity(), v)
    }
    protected fun setUniformValue(handle: Int, v: Array<Int>) = setUniformValue(handle, v.toIntArray())

    protected fun setUniformValue(handle: Int, v: FloatArray) {
        if (handle != GLES32.GL_INVALID_INDEX) GLES32.glUniform1fv(handle, v.size, v, 0)
    }
    protected fun setUniformValue(handle: Int, v: FloatBuffer) {
        if (handle != GLES32.GL_INVALID_INDEX) GLES32.glUniform1fv(handle, v.capacity(), v)
    }
    protected fun setUniformValue(handle: Int, v: Array<Float>) = setUniformValue(handle, v.toFloatArray())

    fun<T : BaseVec<*>> setUniformValue(nom: String, v: Array<T>) {
        if (!v.isEmpty()) {
            val vecSize = v[0].size
            val array = FloatArray(v.size * vecSize) { i -> v[i / vecSize].data[i % vecSize] }

            setUniformValue(nom, array)
        }
    }
    fun<T : BaseMat<*,*>> setUniformValue(nom: String, v: Array<T>) {
        if (!v.isEmpty()) {
            val matSize = v[0].size * v[0].size
            val array = FloatArray(v.size * matSize) { i -> v[i / matSize].data[i % matSize] }

            setUniformValue(nom, array)
        }
    }

    fun setUniformValue(nom: String, v: Any?) {
        if (v != null) {
            when (v) {
                is Int   -> GLES32.glUniform1i(getUniformLocation(nom), v)
                is Float -> GLES32.glUniform1f(getUniformLocation(nom), v)

                is Vec2 -> GLES32.glUniform2f(getUniformLocation(nom), v.x, v.y)
                is Vec3 -> GLES32.glUniform3f(getUniformLocation(nom), v.x, v.y, v.z)
                is Vec4 -> GLES32.glUniform4f(getUniformLocation(nom), v.x, v.y, v.z, v.a)

                is Mat2 -> GLES32.glUniformMatrix2fv(getUniformLocation(nom), 1, false, v.data, 0)
                is Mat3 -> GLES32.glUniformMatrix3fv(getUniformLocation(nom), 1, false, v.data, 0)
                is Mat4 -> GLES32.glUniformMatrix4fv(getUniformLocation(nom), 1, false, v.data, 0)

                is Structure -> v.toUniform(nom, this)

                is Color -> GLES32.glUniform3f(getUniformLocation(nom), v.r, v.g, v.b)

                is Boolean -> GLES32.glUniform1i(getUniformLocation(nom), if (v) 1 else 0)

                else -> throw RuntimeException("Unsupported value type : ${v::class.qualifiedName}")
            }

            GLUtils.checkGlError("Load uniform $nom")
        }
    }
}