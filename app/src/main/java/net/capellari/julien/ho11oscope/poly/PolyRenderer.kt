package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Vec3
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PolyRenderer(val context: Context): GLSurfaceView.Renderer {
    // Companion
    companion object {
        // Attributs
        const val TAG = "PolyRenderer"

        // - camera field view angle (degrees)
        const val FOV_Y: Float = 60f

        // - clipping plane
        const val NEAR_CLIP: Float = 0.1f
        const val FAR_CLIP:  Float = 1000f

        // Model spin speed (deg / s)
        const val MODEL_ROTATION_SPEED: Float = 45f

        // Camera positions and orientation
        val EYE =    Vec3(0f, 3f, -10f)
        val TARGET = Vec3(0f, 0f, 0f)
        val UP =     Vec3(0f, 1f, 0f)
    }

    // Attributs
    private var polyProgram: PolyProgram = PolyProgram.getInstance()
    private var readyToRender = false

    private var indexCount: Int = 0
    private var lastFrameTime: Long = 0
    private var angle: Float = 0f // current rotation angle (deg)

    @Volatile
    var asset: Asset? = null // object to render
        set(asset) {
            field = asset
            readyToRender = false
            Log.d(TAG, "Recieved new object to render")
        }

    // Méthodes
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0.15f, 0.15f, 1f)
        disactivateOpacity()

        lastFrameTime = System.currentTimeMillis()
        polyProgram.compile(context)
    }

    override fun onDrawFrame(unused: GL10?) {
        // Update spin animation
        val now    = System.currentTimeMillis()
        val deltaT = minOf((now - lastFrameTime) * 0.001f, 0.1f)

        lastFrameTime = now
        angle += deltaT * MODEL_ROTATION_SPEED

        // Draw background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // model matrix rotate around Y axis
        polyProgram.mMatrix = Mat4.rotate(angle, 0f, 1f, 0f)

        // Compute MVP Matrix
        polyProgram.mvpMatrix = polyProgram.pMatrix * (polyProgram.vMatrix * polyProgram.mMatrix)

        // render
        if (readyToRender) {
            polyProgram.render(indexCount)
        } else {
            asset?.also {
                // Prepare rendering
                indexCount = it.indexCount
                polyProgram.indices = it.indices

                polyProgram.positions = it.positions
                polyProgram.normals   = it.normals
                polyProgram.ambientColors  = it.ambientColors
                polyProgram.diffuseColors  = it.diffuseColors
                polyProgram.specularColors = it.specularColors
                polyProgram.specularExps = it.specularExps
                polyProgram.opacities    = it.opacities

                // Ready !
                readyToRender = true
                Log.d(TAG, "VBOs/IBO created : ready to render")
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        polyProgram.pMatrix = Mat4.perspective(FOV_Y, ratio, NEAR_CLIP, FAR_CLIP)
    }

    fun activateOpacity() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun disactivateOpacity() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(true)

        GLES20.glDisable(GLES20.GL_BLEND)
    }
}