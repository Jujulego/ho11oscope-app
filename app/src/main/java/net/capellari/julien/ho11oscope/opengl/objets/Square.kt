package net.capellari.julien.ho11oscope.opengl.objets

import android.opengl.GLES20
import net.capellari.julien.opengl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// Valeurs
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
abstract class Square : BaseProgram() {
    // Companion
    companion object {
        private var instance: Square? = null

        fun getInstance(): Square = instance ?: synchronized(this) {
            instance ?: BaseProgram.getImplementation<Square>().also { instance = it }
        }
    }

    // Attributs
    @Uniform("uMVPMatrix") open var mvpMatrix = Mat4()
    @Uniform("vColor")     open var color     = Vec4(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    @Attribute("vPosition") open var coords: ArrayList<Vec3>? = arrayListOf(
            Vec3(-.5f,  .5f, 0f),
            Vec3(-.5f, -.5f, 0f),
            Vec3( .5f, -.5f, 0f),
            Vec3( .5f,  .5f, 0f)
    )

    @IBO open var drawOrder: Array<Short>? = arrayOf(0, 1, 2, 0, 2, 3)
}