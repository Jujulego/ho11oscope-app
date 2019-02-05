package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseProgram
import net.capellari.julien.opengl.base.BufferObject

class PointLight() : Light() {
    /** @class: PointLight
     * @brief: Structure to communicate point light data to GPU
     *
     * GLSL equivalent structure :
     * struct PointLight {
     *     vec3 color;
     *
     *     float ambient;
     *     float diffuse;
     *     float specular;
     *
     *     vec3 position;
     *
     *     float constant;
     *     float linear;
     *     float quadratic;
     * };
     */

    // Attributs
    var position: Vec3 = Vec3()

    var constant:  Float = 1f
    var linear:    Float = .045f
    var quadratic: Float = .0075f

    // Constructeurs
    constructor(pos: Vec3): this() { position = pos }
    constructor(x: Float, y: Float, z: Float): this(Vec3(x, y, z))

    // Méthodes
    override fun getBufferSize(): Int {
        return super.getBufferSize() + (3 + 1 + 1 + 1) * GLUtils.FLOAT_SIZE
    }

    override fun toBuffer(buffer: BufferObject) {
        super.toBuffer(buffer)

        buffer.put(position)

        buffer.put(constant)
        buffer.put(linear)
        buffer.put(quadratic)
    }

    override fun toUniform(nom: String, program: BaseProgram) {
        program.usingProgram {
            super.toUniform(nom, program)

            program.setUniformValue("$nom.position", position)

            program.setUniformValue("$nom.constant",  constant)
            program.setUniformValue("$nom.linear",    linear)
            program.setUniformValue("$nom.quadratic", quadratic)
        }
    }
}