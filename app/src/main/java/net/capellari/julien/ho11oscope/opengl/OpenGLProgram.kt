package net.capellari.julien.ho11oscope.opengl

import net.capellari.julien.opengl.*

// Valeurs
@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX, "",
            """
                uniform mat4 uMVPMatrix;
                attribute vec4 vPosition;
                void main() {
                    gl_Position = uMVPMatrix * vPosition;
                }
            """
        ),
        ShaderScript(ShaderType.FRAGMENT, "",
            """
                precision mediump float;
                uniform vec4 vColor;
                void main() {
                    gl_FragColor = vColor;
                }
            """
        )
    ),
    attributs = [
        Attribute(AttributeType.VERTICES, "vPosition")
    ]
)
abstract class OpenGLProgram : BaseProgram() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseProgram.getImplementation<OpenGLProgram>() }
    }

    // Attributs
    @Uniform("uMVPMatrix") open var mvpMatrix = Mat4()
    @Uniform("vColor")     open var color     = Vec4(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
}