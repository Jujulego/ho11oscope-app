package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*

@Program(
    shaders = [
        ShaderScript(ShaderType.VERTEX,   file = "shaders/vertex.glsl"),
        ShaderScript(ShaderType.FRAGMENT, file = "shaders/fragment.glsl")
    ]
)
abstract class PolyProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<PolyProgram>() }
    }

    // Attributs
    var pMatrix = Mat4()

    // - buffers
    @Indices open var indices: ArrayList<Int>? = null

    // - uniforms
    @Uniform("uMVP")   open var mvpMatrix       = Mat4()
    @Uniform("uM")     open var mMatrix         = Mat4()
    @Uniform("uV")     open var vMatrix         = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Uniform("uLight") open var lightPosition   = Vec3(10f, 0f, 0f)
    @Uniform("uLightPower") open var lightPower = 500f

    @Uniform("uAmbientFactor")  open var ambientFactor  = .1f
    @Uniform("uDiffuseFactor")  open var diffuseFactor  = .7f
    @Uniform("uSpecularFactor") open var specularFactor = .5f

    // - attributes
    @Attribute("aPosition")      open var positions:      ArrayList<Vec3>? = null
    @Attribute("aNormal")        open var normals:        ArrayList<Vec3>? = null
    @Attribute("aAmbientColor")  open var ambientColors:  ArrayList<Vec3>? = null
    @Attribute("aDiffuseColor")  open var diffuseColors:  ArrayList<Vec3>? = null
    @Attribute("aSpecularColor") open var specularColors: ArrayList<Vec3>? = null
    @Attribute("aSpecularExp")   open var specularExps:  ArrayList<Float>? = null
    @Attribute("aOpacity")       open var opacities:     ArrayList<Float>? = null
}