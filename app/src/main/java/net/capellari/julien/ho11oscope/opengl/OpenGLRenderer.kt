package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLES31
import android.opengl.GLSurfaceView
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.base.BaseMesh
import net.capellari.julien.utils.sharedPreference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class OpenGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    // Companion
    companion object {
        // Constantes
        val VITESSE_MIN = 45f
        val INERTIE = .99f
    }

    // Attributs
    @Volatile
    var angle: Float = 0f

    @Volatile
    var vit_angle: Float = VITESSE_MIN

    var lastFrame: Long = 0

    private var vpMatrix   = Mat4()
    private val viewMatrix = Mat4.lookAt(
            0f, 0f, -3f,
            0f, 0f, 0f,
            0f, 1f, 0f)

    val program = OpenGLProgram.instance
    var mesh: BaseMesh = Triangle()
    var newMesh = true

    // Propriétés
    private var wireRendering by sharedPreference("wireframe_rendering", context, false)

    // Events
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        program.compile(context)

        if (wireRendering) {
            program.mode = GLES31.GL_LINE_LOOP
        } else {
            program.mode = GLES31.GL_TRIANGLES
        }

        lastFrame = System.currentTimeMillis()
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)

        val now    = System.currentTimeMillis()
        val deltaT = minOf((now - lastFrame) * 0.001f, 0.1f)

        lastFrame = now

        angle += vit_angle * deltaT
        if (abs(vit_angle) > VITESSE_MIN) vit_angle *= INERTIE

        program.mvpMatrix = vpMatrix * Mat4.rotate(angle, 0f, 0f, -1f)

        if (newMesh) {
            program.prepare(mesh)
            newMesh = false
        }

        program.render(mesh)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES31.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        vpMatrix = Mat4.frustum(-ratio, ratio, -1f, 1f, 3f, 7f) * viewMatrix
    }

    // Méthodes
    fun setupTriangle() {
        mesh = Triangle()
        newMesh = true
    }
    fun setupCarre() {
        mesh = Carre()
        newMesh = true
    }
    fun setupHexagone() {
        mesh = Hexagone()
        newMesh = true
    }

    // Meshes
    class Triangle : BaseMesh(false, false) {
        override fun getMaterial(): Material {
            return Material("")
        }

        override fun getVertices(): Any = arrayOf(
                Vec3(  0f,  0.622008459f, 0f),
                Vec3(-.5f, -0.311004243f, 0f),
                Vec3( .5f, -0.311004243f, 0f)
        )
    }
    class Carre    : BaseMesh(true,  false) {
        override fun getMaterial(): Material {
            return Material("")
        }

        override fun getIndices(): Any = arrayOf<Short>(0, 1, 2, 0, 2, 3)

        override fun getVertices(): Any = arrayOf(
                Vec3(-.5f,  .5f, 0f),
                Vec3(-.5f, -.5f, 0f),
                Vec3( .5f, -.5f, 0f),
                Vec3( .5f,  .5f, 0f)
        )
    }
    class Hexagone : BaseMesh(true,  false) {
        override fun getMaterial(): Material {
            return Material("")
        }

        override fun getIndices(): Any = arrayOf<Short>(
                0, 1, 2,
                0, 2, 3,
                0, 3, 4,
                0, 4, 5,
                0, 5, 6,
                0, 6, 1
        )

        override fun getVertices(): Any = arrayOf(
                Vec3(   0f,    0f, 0f), // 0
                Vec3(  .5f,    0f, 0f), // 1
                Vec3( .25f,  .43f, 0f), // 2
                Vec3(-.25f,  .43f, 0f), // 3
                Vec3( -.5f,    0f, 0f), // 4
                Vec3(-.25f, -.43f, 0f), // 5
                Vec3( .25f, -.43f, 0f)  // 6
        )
    }
}