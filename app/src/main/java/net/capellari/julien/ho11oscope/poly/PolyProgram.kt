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
        private var instance: PolyProgram? = null

        fun getInstance(): PolyProgram = instance ?: synchronized(this) {
            instance ?: BaseProgram.getImplementation<PolyProgram>().also { instance = it }
        }
    }

    // Attributs
    var pMatrix = Mat4()

    // - buffers
    @IBO open var indices: ArrayList<Int>? = null

    // - uniforms
    @Uniform("uMVP")   open var mvpMatrix       = Mat4()
    @Uniform("uM")     open var mMatrix         = Mat4()
    @Uniform("uV")     open var vMatrix         = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Uniform("uLight") open var lightPosition   = Vec3(0f, 2f, 25f)
    @Uniform("uLightPower") open var lightPower = 600f

    // - attributes
    @Attribute("aPosition")      open var positions:      ArrayList<Vec3>? = null
    @Attribute("aNormal")        open var normals:        ArrayList<Vec3>? = null
    @Attribute("aAmbientColor")  open var ambientColors:  ArrayList<Vec3>? = null
    @Attribute("aDiffuseColor")  open var diffuseColors:  ArrayList<Vec3>? = null
    @Attribute("aSpecularColor") open var specularColors: ArrayList<Vec3>? = null
    @Attribute("aSpecularExp")   open var specularExps:  ArrayList<Float>? = null
    @Attribute("aOpacity")       open var opacities:     ArrayList<Float>? = null
}