package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import net.capellari.julien.ho11oscope.poly.opengl.GLUtils
import net.capellari.julien.ho11oscope.poly.opengl.Shaders
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PolyRenderer(val context: Context): GLSurfaceView.Renderer {
    // Companion
    companion object {
        // Attributs
        const val TAG = "PolyRendere"

        // - camera field view angle (degrees)
        const val FOV_Y: Float = 60f

        // - clipping plane
        const val NEAR_CLIP: Float = 0.1f
        const val FAR_CLIP:  Float = 1000f

        // Model spin speed (deg / s)
        const val MODEL_ROTATION_SPEED: Float = 45f

        // Camera position and orientation
        const val EYE_X: Float = 0f
        const val EYE_Y: Float = 3f
        const val EYE_Z: Float = -10f
        const val TARGET_X: Float = 0f
        const val TARGET_Y: Float = 0f
        const val TARGET_Z: Float = 0f
        const val UP_X: Float = 0f
        const val UP_Y: Float = 1f
        const val UP_Z: Float = 0f
    }

    // Attributs
    private lateinit var shaders: Shaders
    private var readyToRender = false

    private var positionVbo: Int = 0
    private var colorVbo: Int = 0
    private var ibo: Int = 0

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

    // - matrices
    private val modelMatrix = FloatArray(16) // object space => world space
    private val viewMatrix  = FloatArray(16) // world space  => eye space
    private val projMatrix  = FloatArray(16) // eye space    => clip space
    private val mvpMatrix   = FloatArray(16) // model * view * proj

    // Méthodes
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0.15f, 0.15f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        lastFrameTime = System.currentTimeMillis()
        shaders = Shaders(context)
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
        Matrix.setRotateM(modelMatrix, 0, angle, 0f, 1f, 0f)

        // camera position
        Matrix.setLookAtM(viewMatrix, 0,
                EYE_X,    EYE_Y,    EYE_Z,
                TARGET_X, TARGET_Y, TARGET_Z,
                UP_X,     UP_Y,     UP_Z
        )

        // Compute MVP Matrix
        FloatArray(16).let {
            Matrix.multiplyMM(it, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, it, 0)
        }

        // render
        if (readyToRender) {
            shaders.render(mvpMatrix, indexCount, ibo, positionVbo, colorVbo)
        } else {
            asset?.also {
                // Prepare rendering
                indexCount = it.indexCount
                ibo = GLUtils.createIbo(it.indices)

                positionVbo = GLUtils.createVbo(it.positions)
                colorVbo = GLUtils.createVbo(it.colors)

                // Ready !
                readyToRender = true
                Log.d(TAG, "VBOs/IBO created : ready to render")
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, FOV_Y, ratio, NEAR_CLIP, FAR_CLIP)
    }
}