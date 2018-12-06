package net.capellari.julien.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    // Méthodes
    override fun toBuffer(buffer: BaseBufferObject) {
        buffer.put(ambientColor)
        buffer.put(diffuseColor)
        buffer.put(specularColor)
        buffer.put(specularExp)
        buffer.put(opacity)
        buffer.put(texture != null)
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
        }
    }

    fun loadTexture(context: Context) : Bitmap {
        val file = File(context.filesDir, texture)
        return BitmapFactory.decodeFile(file.absolutePath)
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
}