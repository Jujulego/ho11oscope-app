package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.base.Program

@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment.glsl"),
        ShaderScript(ShaderType.GEOMETRY, file = "shaders/geometry.glsl")
    ),
    attributs = [
        Attribute("aPosition", AttributeType.VERTICES),
        Attribute("aNormal",   AttributeType.NORMALS),
        Attribute("aTexCoord", AttributeType.TEXCOORDS)
    ]
)
abstract class PolyProgram : Program() {
    // Companion
    companion object {
        val instance by lazy(this) { Program.getImplementation<PolyProgram>() }
    }

    // Attributs
    @Uniform("magnitude") open var magnitude = .5f
    @Uniform("material", true) var material  = Material("")

    @UniformBlock("Matrices")   val matrices   = MatricesBlock.instance
    @UniformBlock("Stables")    val stables    = StablesBlock.instance
    @UniformBlock("Parameters") val parameters = ParametersBlock.instance

    @ShaderStorage("Lights") open var lights = LightsBlock.instance
}