package net.capellari.julien.opengl.mtl

import android.util.Log
import net.capellari.julien.opengl.GLUtils
import net.capellari.julien.opengl.Struct
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.buffers.BufferObject

data class Material(val name: String) : Struct() {
    // Attributs
    var ambientColor = Vec3(1f, 1f, 1f)
    var diffuseColor = Vec3(1f, 1f, 1f)
    var specularColor = Vec3(1f, 1f, 1f)

    var specularExp = 1f
    var opacity = 1f

    // Méthodes
    override fun toBuffer(buffer: BufferObject) {
        buffer.put(ambientColor)
        buffer.put(diffuseColor)
        buffer.put(specularColor)
        buffer.put(specularExp)
        buffer.put(opacity)
    }

    override fun getBufferSize(): Int {
        return (3 + 3 + 3 + 1 + 1) * GLUtils.FLOAT_SIZE
    }

    fun print() {
        Log.d("MtlLibrary", "Material $name:")
        Log.d("MtlLibrary", "   Ka: $ambientColor")
        Log.d("MtlLibrary", "   Kd: $diffuseColor")
        Log.d("MtlLibrary", "   Ks: $specularColor")
        Log.d("MtlLibrary", "   Ns: %.3f".format(specularExp))
        Log.d("MtlLibrary", "   d:  %.3f".format(opacity))
    }
}