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
    @IBO(BufferType.INT)   var indices: IntBuffer?   = null
    @VBO(BufferType.FLOAT) var vbo:     FloatBuffer? = null

    // - uniforms
    @Uniform("uMVP")        var mvpMatrix     = Mat4()
    @Uniform("uM")          var mMatrix       = Mat4()
    @Uniform("uV")          var vMatrix       = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Uniform("uLight")      var lightPosition = Vec3(0f, 2f, 25f)
    @Uniform("uLightPower") var lightPower    = 600f

    // - attributes
    @Attribute("aPosition",      vbo=GLUtils.COORDS_PER_VERTEX)    var positions:      FloatBuffer? = null
    @Attribute("aNormal",        vbo=GLUtils.COORDS_PER_VERTEX)    var normals:        FloatBuffer? = null
    @Attribute("aAmbientColor",  vbo=GLUtils.NUM_COLOR_COMPONENTS) var ambientColors:  FloatBuffer? = null
    @Attribute("aDiffuseColor",  vbo=GLUtils.NUM_COLOR_COMPONENTS) var diffuseColors:  FloatBuffer? = null
    @Attribute("aSpecularColor", vbo=GLUtils.NUM_COLOR_COMPONENTS) var specularColors: FloatBuffer? = null
    @Attribute("aSpecularExp",   vbo=1) var specularExps: FloatBuffer? = null
    @Attribute("aOpacity",       vbo=1) var opacities:    FloatBuffer? = null
}