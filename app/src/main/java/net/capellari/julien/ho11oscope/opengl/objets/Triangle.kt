package net.capellari.julien.ho11oscope.opengl.objets

import net.capellari.julien.opengl.*

@Program(
        shaders = [
            ShaderScript(ShaderType.VERTEX,
                    script = """
                        uniform mat4 uMVPMatrix;
                        attribute vec4 vPosition;
                        void main() {
                            gl_Position = uMVPMatrix * vPosition;
                        }
                    """
            ),
            ShaderScript(ShaderType.FRAGMENT,
                    script = """
                        precision mediump float;
                        uniform vec4 vColor;
                        void main() {
                            gl_FragColor = vColor;
                        }
                    """
            )
        ]
)
abstract class Triangle : BaseProgram() {
    // Companion
    companion object {
        private var instance: Triangle? = null

        fun getInstance(): Triangle = instance ?: synchronized(this) {
            instance ?: BaseProgram.getImplementation<Triangle>().also { instance = it }
        }
    }

    // Attributs
    @Uniform("uMVPMatrix") open var mvpMatrix = Mat4()
    @Uniform("vColor")     open var color     = Vec4(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    @Attribute("vPosition") open var coords: ArrayList<Vec3>? = arrayListOf(
            Vec3(  0f,  0.622008459f, 0f),
            Vec3(-.5f, -0.311004243f, 0f),
            Vec3( .5f, -0.311004243f, 0f)
    )
}