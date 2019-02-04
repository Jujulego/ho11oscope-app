package net.capellari.julien.opengl

import android.graphics.BitmapFactory
import android.opengl.GLES32
import android.util.Log
import net.capellari.julien.opengl.base.BufferObject
import net.capellari.julien.opengl.base.BaseProgram
import net.capellari.julien.opengl.base.Structure

class Material(val name: String) : Structure {
    /** @class: Material
     * @brief: Structure to communicate material data to GPU
     *
     * GLSL equivalent structure :
     * struct Material {
     *     vec3 ambientColor;
     *     vec3 diffuseColor;
     *     vec3 specularColor;
     *
     *     float specularExp;
     *     float opacity;
     *
     *     int hasTexture;
     *     sampler2D texture;
     * };
     */

    // Attributs
    var ambientColor  = Color.WHITE
    var diffuseColor  = Color.WHITE
    var specularColor = Color.WHITE

    var specularExp = 1f
    var opacity = 1f

    var texture: String? = null
    var texHandler: Int = GLES32.GL_INVALID_INDEX

    // Méthodes
    override fun toBuffer(buffer: BufferObject) {
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
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texHandler)
    }
    internal fun loadTexture() {
        texture?.let { tex ->
            // Load file
            val bitmap = BitmapFactory.decodeFile(tex,
                    BitmapFactory.Options().apply {
                        inScaled = false
                    }
            )

            // Create texture
            texHandler = IntArray(1).also { GLES32.glGenTextures(1, it, 0) }[0]
            bindTexture()

            // Parameters
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST)

            // Push data
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
            //GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D)

            bitmap.recycle()
        }
    }
}