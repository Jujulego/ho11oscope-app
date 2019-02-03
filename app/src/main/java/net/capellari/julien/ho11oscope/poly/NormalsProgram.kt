package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.base.Program

@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex_normals.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment_normals.glsl"),
        ShaderScript(ShaderType.GEOMETRY, file = "shaders/geometry_normals.glsl")
    ),
    attributs = [
        Attribute("aPos",    AttributeType.VERTICES),
        Attribute("aNormal", AttributeType.NORMALS)
    ]
)
abstract class NormalsProgram : Program() {
    // Companion
    companion object {
        val instance by lazy(this) { Program.getImplementation<NormalsProgram>() }
    }

    // Attributs
    // - uniforms
    @UniformBlock("Matrices") val matrices = MatricesBlock.instance
    @UniformBlock("Stables")  val stables  = StablesBlock.instance
}