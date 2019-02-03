package net.capellari.julien.ho11oscope.opengl

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.base.Program

// Valeurs
@Program(
    shaders = Shaders(
        ShaderScript(ShaderType.VERTEX, "",
            """ #version 100
                uniform mat4 uMVPMatrix;
                attribute vec4 vPosition;
                attribute vec3 vColor;

                varying vec3 color;

                void main() {
                    gl_Position = uMVPMatrix * vPosition;
                    color = vColor;
                }
            """
        ),
        ShaderScript(ShaderType.FRAGMENT, "",
            """ #version 100
                precision mediump float;
                varying vec3 color;
                void main() {
                    gl_FragColor = vec4(color, 0);
                }
            """
        )
    ),
    attributs = [
        Attribute("vPosition", AttributeType.VERTICES),
        Attribute("vColor")
    ]
)
abstract class OpenGLProgram : Program() {
    // Companion
    companion object {
        val instance by lazy(this) { Program.getImplementation<OpenGLProgram>() }
    }

    // Attributs
    @Uniform("uMVPMatrix") open var mvpMatrix = Mat4()
}