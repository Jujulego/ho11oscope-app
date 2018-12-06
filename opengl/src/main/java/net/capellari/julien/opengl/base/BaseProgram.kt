package net.capellari.julien.opengl.base

import android.content.Context
import android.opengl.GLES31
import android.util.Log
import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.buffers.UniformBufferObject
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
    var mode: Int = GLES31.GL_TRIANGLES
    var defaultMode: Int = GLES31.GL_TRIANGLES
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
    protected abstract fun enableVBO(mesh: BaseMesh)
    protected abstract fun setMeshMaterial(mesh: BaseMesh)

    // Méthodes
    fun compile(context: Context) {
        // Compile program
        program = GLES31.glCreateProgram()
        loadShaders(context, program)

        // Link program
        GLES31.glLinkProgram(program)

        GLUtils.checkGlError("Linking program")
        Log.d(TAG, "Program linked")

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

    fun prepare(context: Context, mesh: BaseMesh) = prepare(context, arrayListOf(mesh))
    fun prepare(context: Context, meshes: Collection<BaseMesh>) {
        usingProgram {
            for (mesh in meshes) {
                mesh.genBuffers()
                mesh.loadTexture(context)
            }
        }
    }

    fun render(mesh: BaseMesh) = render(arrayListOf(mesh))
    fun render(meshes: Collection<BaseMesh>) {
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
                    GLES31.glUseProgram(program)
                    isActive = true
                }
            }

            return lambda()

        } finally {
            // Disactivate program
            if (wasActive == false) {
                synchronized(this) {
                    GLES31.glUseProgram(0)
                    isActive = false
                }
            }
        }
    }

    // - shaders
    protected fun loadShader(script: String, type: ShaderType, file: String? = null): Int {
        // Create shader
        val shader = GLES31.glCreateShader(GLUtils.getGlShaderType(type))
        GLES31.glShaderSource(shader, script)
        GLUtils.checkGlError("Creating shader ${file ?: "<input>"}")

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
        //} else {
        //    Log.d(TAG, "Uniform $name : $handle")
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

        Log.d(TAG, "UniformBlock $name : $index => $binding")
        GLES31.glUniformBlockBinding(program, index, binding)
        GLUtils.checkGlError("Getting UniformBlock $name")
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
                        1, IntArray(1) { GLES31.GL_BUFFER_BINDING }, 0,
                        1, IntArray(1) { 1 }, 0, it, 0)
            }[0]

        Log.d(TAG, "ShaderStorage $name : $index => $binding")
        GLUtils.checkGlError("Getting ShaderStorage $name")

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
            is BaseStructure -> v.getBufferSize()

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
                is Int   -> GLES31.glUniform1i(getUniformLocation(nom), v)
                is Float -> GLES31.glUniform1f(getUniformLocation(nom), v)

                is Vec2 -> GLES31.glUniform2f(getUniformLocation(nom), v.x, v.y)
                is Vec3 -> GLES31.glUniform3f(getUniformLocation(nom), v.x, v.y, v.z)
                is Vec4 -> GLES31.glUniform4f(getUniformLocation(nom), v.x, v.y, v.z, v.a)

                is Mat2 -> GLES31.glUniformMatrix2fv(getUniformLocation(nom), 1, false, v.data, 0)
                is Mat3 -> GLES31.glUniformMatrix3fv(getUniformLocation(nom), 1, false, v.data, 0)
                is Mat4 -> GLES31.glUniformMatrix4fv(getUniformLocation(nom), 1, false, v.data, 0)

                is BaseStructure -> v.toUniform(nom, this)

                is Boolean -> GLES31.glUniform1i(getUniformLocation(nom), if (v) 1 else 0)

                else -> throw RuntimeException("Unsupported value type : ${v::class.qualifiedName}")
            }

            GLUtils.checkGlError("Load uniform $nom")
        }
    }
}