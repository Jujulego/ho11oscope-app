package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

@Program(
    shaders = [
        ShaderScript(file = "shaders/vertex.glsl",   type = ShaderType.VERTEX),
        ShaderScript(file = "shaders/fragment.glsl", type = ShaderType.FRAGMENT)
    ]
)
abstract class PolyProgram : BaseProgram() {
    // Companion
    companion object {
        private var instance: PolyProgram? = null

        fun getInstance(): PolyProgram = instance ?: synchronized(this) {
            instance ?: BaseProgram.getImplementation(PolyProgram::class).also { instance = it }
        }
    }

    // Attributs
    var pMatrix = Mat4()

    // - buffers
    @IBO open var indices: IntBuffer? = null

    // - uniforms
    @Uniform("uMVP")   open var mvpMatrix       = Mat4()
    @Uniform("uM")     open var mMatrix         = Mat4()
    @Uniform("uV")     open var vMatrix         = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Uniform("uLight") open var lightPosition   = Vec3(0f, 2f, 25f)
    @Uniform("uLightPower") open var lightPower = 600f

    // - attributes
    @VBO @Attribute("aPosition")      open var positions:      ArrayList<Vec3>? = null
    @VBO @Attribute("aNormal")        open var normals:        ArrayList<Vec3>? = null
    @VBO @Attribute("aAmbientColor")  open var ambientColors:  ArrayList<Vec3>? = null
    @VBO @Attribute("aDiffuseColor")  open var diffuseColors:  ArrayList<Vec3>? = null
    @VBO @Attribute("aSpecularColor") open var specularColors: ArrayList<Vec3>? = null
    @VBO @Attribute("aSpecularExp")   open var specularExps:  ArrayList<Float>? = null
    @VBO @Attribute("aOpacity")       open var opacities:     ArrayList<Float>? = null
}