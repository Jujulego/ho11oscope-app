package net.capellari.julien.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES31
import android.util.Log
import net.capellari.julien.opengl.base.BaseBufferObject
import net.capellari.julien.opengl.base.BaseProgram
import net.capellari.julien.opengl.base.BaseStructure
import java.io.File

class Material(val name: String) : BaseStructure {
    // Attributs
    var ambientColor = Vec3(1f, 1f, 1f)
    var diffuseColor = Vec3(1f, 1f, 1f)
    var specularColor = Vec3(1f, 1f, 1f)

    var specularExp = 1f
    var opacity = 1f

    var texture: String? = null
    var texHandler: Int = GLES31.GL_INVALID_INDEX

    // Méthodes
    override fun toBuffer(buffer: BaseBufferObject) {
        buffer.put(ambientColor)
        buffer.put(diffuseColor)
        buffer.put(specularColor)
        buffer.put(specularExp)
        buffer.put(opacity)
        buffer.put(texture != null)
        buffer.put(0)
    }

    override fun getBufferSize(): Int {
        return (3 + 3 + 3 + 1 + 1) * GLUtils.FLOAT_SIZE + 1 * GLUtils.INT_SIZE
    }

    override fun toUniform(nom: String, program: BaseProgram) {
        program.usingProgram {
            program.setUniformValue("$nom.ambientColor",  ambientColor)
            program.setUniformValue("$nom.diffuseColor",  diffuseColor)
            program.setUniformValue("$nom.specularColor", specularColor)
            program.setUniformValue("$nom.specularExp",   specularExp)
            program.setUniformValue("$nom.opacity",       opacity)
            program.setUniformValue("$nom.hasTexture",    if (texture != null) 1 else 0)
            program.setUniformValue("$nom.texture",       0)
        }
    }

    fun print() {
        Log.d("MtlLibrary", "Material $name:")
        Log.d("MtlLibrary", "   Ka: $ambientColor")
        Log.d("MtlLibrary", "   Kd: $diffuseColor")
        Log.d("MtlLibrary", "   Ks: $specularColor")
        Log.d("MtlLibrary", "   Ns: %.3f".format(specularExp))
        Log.d("MtlLibrary", "   d:  %.3f".format(opacity))
        texture?.let { Log.d("MtlLibrary", "   T:  $it") }
    }

    internal fun bindTexture() {
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0)
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, texHandler)
    }
    internal fun loadTexture(context: Context) {
        texture?.let { tex ->
            // Load file
            val bitmap = BitmapFactory.decodeFile(
                    File(context.filesDir, tex).absolutePath,
                    BitmapFactory.Options().apply {
                        inScaled = false
                    }
            )

            // Create texture
            texHandler = IntArray(1).also { GLES31.glGenTextures(1, it, 0) }[0]
            bindTexture()

            // Parameters
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE)
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE)
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST)//LINEAR_MIPMAP_LINEAR)
            GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST)//LINEAR)

            // Push data
            android.opengl.GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, bitmap, 0)
            //GLES31.glGenerateMipmap(GLES31.GL_TEXTURE_2D)

            bitmap.recycle()
        }
    }
}