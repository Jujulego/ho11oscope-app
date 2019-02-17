package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.base.BaseProgram

@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment.glsl"),
        ShaderScript(ShaderType.GEOMETRY, file = "shaders/geometry_wireframe.glsl")
    ),
    attributs = [
        Attribute("aPosition", AttributeType.VERTICES),
        Attribute("aNormal",   AttributeType.NORMALS),
        Attribute("aTexCoord", AttributeType.TEXCOORDS)
    ]
)
abstract class WireframeProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<WireframeProgram>() }
    }

    // Attributs
    @Uniform("nbLights")  open var nbLights = 0
    @Uniform("lights")    open var lights   = arrayOf<PointLight>()
    @Uniform("magnitude") open var magnitude = .5f
    @Uniform("material", true) var material  = Material("")

    @UniformBlock("Matrices")   val matrices   = MatricesBlock.instance
    @UniformBlock("Stables")    val stables    = StablesBlock.instance
}