package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.Material

@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment.glsl"),
        ShaderScript(ShaderType.GEOMETRY, file = "shaders/geometry.glsl")
    ),
    attributs = [
        Attribute(AttributeType.VERTICES, "aPosition"),
        Attribute(AttributeType.NORMALS,  "aNormal")
    ]
)
abstract class PolyProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<PolyProgram>() }
    }

    // Attributs
    @Uniform("magnitude") open var magnitude = .5f
    @Uniform("material", true) var material  = Material("")

    @UniformBlock("Matrices") open var mvpMatrix   = Mat4()
    @UniformBlock("Matrices") open var modelMatrix = Mat4()

    @UniformBlock("Stables") open var viewMatrix    = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @UniformBlock("Stables") open var projMatrix    = Mat4()
    @UniformBlock("Stables") open var lightPosition = Vec3(10f, 0f, 0f)

    @UniformBlock("Parameters") open var lightPower     = 50f
    @UniformBlock("Parameters") open var ambientFactor  = .1f
    @UniformBlock("Parameters") open var diffuseFactor  = .7f
    @UniformBlock("Parameters") open var specularFactor = .5f
}