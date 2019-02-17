package net.capellari.julien.opengl

import androidx.annotation.CallSuper
import net.capellari.julien.opengl.base.BaseProgram
import net.capellari.julien.opengl.base.BufferObject
import net.capellari.julien.opengl.base.Structure

abstract class Light : Structure {
    /** @class: Light
     * @brief: Base to lights structures
     */

    // Attributs
    var color: Color = Color.WHITE

    var ambient: Float = 1f
    var diffuse: Float = 1f
    var specular: Float = 1f

    var isActive: Boolean = true

    // Méthodes
    @CallSuper
    override fun getBufferSize(): Int {
        return (3 + 1 + 1 + 1) * GLUtils.FLOAT_SIZE + 1 * GLUtils.INT_SIZE
    }

    @CallSuper
    override fun toBuffer(buffer: BufferObject) {
        buffer.put(color)
        buffer.put(ambient)
        buffer.put(diffuse)
        buffer.put(specular)
        buffer.put(isActive)
    }

    @CallSuper
    override fun toUniform(nom: String, program: BaseProgram) {
        program.usingProgram {
            program.setUniformValue("$nom.color",    color)
            program.setUniformValue("$nom.ambient",  ambient)
            program.setUniformValue("$nom.diffuse",  diffuse)
            program.setUniformValue("$nom.specular", specular)
            program.setUniformValue("$nom.isActive", isActive)
        }
    }
}