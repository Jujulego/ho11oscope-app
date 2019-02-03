package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.GLUtils
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.base.BufferObject
import net.capellari.julien.opengl.base.Program
import net.capellari.julien.opengl.base.Structure
import kotlin.math.cos
import kotlin.math.sin

class Light(var distance: Float, var angle: Float, var puissance: Int): Structure {
    // Méthodes
    fun position(y: Float) = Vec3(distance * sin(angle), y, distance * cos(angle))

    // Méthodes
    override fun toBuffer(buffer: BufferObject) {
        buffer.put(position(1f))
        buffer.put(puissance)
    }

    override fun getBufferSize(): Int {
        return (3 + 1) * GLUtils.FLOAT_SIZE
    }

    override fun toUniform(nom: String, program: Program) {
        program.usingProgram {
            program.setUniformValue("$nom.position", position(1f))
            program.setUniformValue("$nom.power",    puissance)
        }
    }
}