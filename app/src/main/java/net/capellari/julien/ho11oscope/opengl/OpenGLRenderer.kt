package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.base.Mesh
import net.capellari.julien.utils.sharedPreference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class OpenGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    // Companion
    companion object {
        // Constantes
        const val VITESSE_MIN = 45f
        const val INERTIE = .99f
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
    var mesh: Mesh = Triangle()
        set(value) {
            field = value
            newMesh = true
        }
    var newMesh = true

    // Propriétés
    private var wireRendering by sharedPreference("wireframe_rendering", context, false)

    // Events
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        program.compile(context)

        if (wireRendering) {
            program.mode = GLES32.GL_LINE_LOOP
        } else {
            program.mode = GLES32.GL_TRIANGLES
        }

        lastFrame = System.currentTimeMillis()
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

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
        GLES32.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        vpMatrix = Mat4.frustum(-ratio, ratio, -1f, 1f, 3f, 7f) * viewMatrix
    }
}