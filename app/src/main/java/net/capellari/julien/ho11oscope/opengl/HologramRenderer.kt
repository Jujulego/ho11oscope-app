package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.preference.PreferenceManager
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Vec3
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HologramRenderer(val context: Context) : GLSurfaceView.Renderer {
    // Attributs
    @Volatile
    var angle: Float = 0f

    private var vpMatrix   = Mat4()
    private val viewMatrix = Mat4.lookAt(0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f)

    val program = HologramProgram.instance

    // Propriétés
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private var transparency: Boolean
        get()  = sharedPreferences.getBoolean("transparency", false)
        set(v) = sharedPreferences.edit().putBoolean("transparency", v).apply()

    private var wireRendering: Boolean
        get()  = sharedPreferences.getBoolean("wire_rendering", false)
        set(v) = sharedPreferences.edit().putBoolean("wire_rendering", v).apply()

    // Events
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        program.compile(context)

        if (wireRendering) {
            program.mode = GLES20.GL_LINE_LOOP
        } else {
            program.mode = GLES20.GL_TRIANGLES
        }
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        program.mvpMatrix = vpMatrix * Mat4.rotate(angle, 0f, 0f, -1f)
        program.render()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        vpMatrix = Mat4.frustum(-ratio, ratio, -1f, 1f, 3f, 7f) * viewMatrix
    }

    // Méthodes
    fun setupTriangle() {
        program.coords = arrayListOf(
                Vec3(  0f,  0.622008459f, 0f),
                Vec3(-.5f, -0.311004243f, 0f),
                Vec3( .5f, -0.311004243f, 0f)
        )
        program.drawOrder = arrayOf(0, 1, 2)
    }
    fun setupCarre() {
        program.coords = arrayListOf(
                Vec3(-.5f,  .5f, 0f),
                Vec3(-.5f, -.5f, 0f),
                Vec3( .5f, -.5f, 0f),
                Vec3( .5f,  .5f, 0f)
        )
        program.drawOrder = arrayOf(0, 1, 2, 0, 2, 3)
    }
}