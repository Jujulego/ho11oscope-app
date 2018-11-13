package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.mtl.Material

@Program(
    shaders = [
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment.glsl"),
        ShaderScript(ShaderType.GEOMETRY, file = "shaders/geometry.glsl")
    ]
)
abstract class PolyProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<PolyProgram>() }
    }

    // Attributs
    @Elements open var indices: ArrayList<Int>? = null

    @ShaderStorage("Materials") open var materials = arrayListOf<Material>()

    // - uniforms
    @Uniform("magnitude") open var magnitude = .5f

    @UniformBlock("Matrices") open var mvpMatrix   = Mat4()
    @UniformBlock("Matrices") open var modelMatrix = Mat4()

    @UniformBlock("Stables") open var viewMatrix    = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @UniformBlock("Stables") open var projMatrix    = Mat4()
    @UniformBlock("Stables") open var lightPosition = Vec3(10f, 0f, 0f)

    @UniformBlock("Parameters") open var lightPower     = 50f
    @UniformBlock("Parameters") open var ambientFactor  = .1f
    @UniformBlock("Parameters") open var diffuseFactor  = .7f
    @UniformBlock("Parameters") open var specularFactor = .5f

    // - attributes
    @Attribute("aPosition") open var positions:   ArrayList<Vec3>? = null
    @Attribute("aNormal")   open var normals:     ArrayList<Vec3>? = null
    @Attribute("aMaterial") open var materialIds: ArrayList<Int>?  = null
}