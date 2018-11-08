package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.preference.PreferenceManager
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.sharedPreference
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
    private var transparency  by sharedPreference("transparency",   context, false)
    private var wireRendering by sharedPreference("wire_rendering", context, false)

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
        program.drawOrder = null
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
    fun setupHexagone() {
        program.coords = arrayListOf(
                Vec3(   0f,    0f, 0f), // 0
                Vec3(  .5f,    0f, 0f), // 1
                Vec3( .25f,  .43f, 0f), // 2
                Vec3(-.25f,  .43f, 0f), // 3
                Vec3( -.5f,    0f, 0f), // 4
                Vec3(-.25f, -.43f, 0f), // 5
                Vec3( .25f, -.43f, 0f)  // 6
        )
        program.drawOrder = arrayOf(
                0, 1, 2,
                0, 2, 3,
                0, 3, 4,
                0, 4, 5,
                0, 5, 6,
                0, 6, 1
        )
    }
}