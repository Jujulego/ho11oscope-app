package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseProgram
import net.capellari.julien.opengl.base.BufferObject

class DirectionalLight : Light() {
    /** @class: DirectionalLight
     * @brief: Structure to communicate directional light data to GPU
     *
     * GLSL equivalent structure :
     * struct DirectionalLight {
     *     vec3 color;
     *
     *     float ambient;
     *     float diffuse;
     *     float specular;
     *
     *     vec3 direction;
     * };
     */

    // Attributs
    var direction: Vec3 = Vec3()

    // Méthodes
    override fun getBufferSize(): Int {
        return super.getBufferSize() + 3 * GLUtils.FLOAT_SIZE
    }

    override fun toBuffer(buffer: BufferObject) {
        super.toBuffer(buffer)

        buffer.put(direction)
    }

    override fun toUniform(nom: String, program: BaseProgram) {
        program.usingProgram {
            super.toUniform(nom, program)
            program.setUniformValue("$nom.direction", direction)
        }
    }
}