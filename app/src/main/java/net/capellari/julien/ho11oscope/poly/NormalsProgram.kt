package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.base.BaseProgram

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
abstract class NormalsProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<NormalsProgram>() }
    }

    // Attributs
    // - uniforms
    @Uniform("projection") open var projection = Mat4()
    @Uniform("view")       open var view       = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Uniform("model")      open var model      = Mat4()
}