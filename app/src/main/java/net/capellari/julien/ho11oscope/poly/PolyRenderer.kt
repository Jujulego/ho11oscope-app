package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.preference.PreferenceManager
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.sharedPreference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PolyRenderer(val context: Context): GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener {
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
        val TARGET = Vec3(0f, 0f,  0f)
        val UP =     Vec3(0f, 1f,  0f)
    }

    // Attributs
    private var polyProgram: PolyProgram = PolyProgram.instance
    private var readyToRender = false

    @Volatile
    private var setupChange = true

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

    // Propriétés
    private var transparency  by sharedPreference("transparency",   context, false)
    private var wireRendering by sharedPreference("wire_rendering", context, false)

    private var ambientFactor  by sharedPreference("ambientFactor",  context, 50)
    private var diffuseFactor  by sharedPreference("diffuseFactor",  context, 50)
    private var specularFactor by sharedPreference("specularFactor", context, 50)

    private var lightPower by sharedPreference("lightPower", context, 500)

    // Méthodes
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        GLES31.glClearColor(0f, 0.15f, 0.15f, 1f)

        lastFrameTime = System.currentTimeMillis()
        polyProgram.compile(context)

        // Setup
        setupRenderingMode()

        // Events
        PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDrawFrame(unused: GL10?) {
        // Setup changes
        if (setupChange) {
            setupTransparency()
            setupColorFactors()
            setupLightPower()

            setupChange = false
        }

        // Update spin animation
        val now    = System.currentTimeMillis()
        val deltaT = minOf((now - lastFrameTime) * 0.001f, 0.1f)

        lastFrameTime = now
        angle += deltaT * MODEL_ROTATION_SPEED

        // Draw background
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)

        // model matrix rotate around Y axis
        polyProgram.modelMatrix = Mat4.rotate(angle, 0f, 1f, 0f)

        // Compute MVP Matrix
        polyProgram.mvpMatrix = polyProgram.projMatrix * (polyProgram.viewMatrix * polyProgram.modelMatrix)

        // render
        if (readyToRender) {
            polyProgram.render()
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
        GLES31.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        polyProgram.projMatrix = Mat4.perspective(FOV_Y, ratio, NEAR_CLIP, FAR_CLIP)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d(TAG, "Poly setup : updated $key")
        when(key) {
            this::wireRendering.sharedPreference -> setupRenderingMode()
            this::lightPower.sharedPreference    -> setupLightPower()

            this::transparency.sharedPreference,
            this::ambientFactor.sharedPreference,
            this::diffuseFactor.sharedPreference,
            this::specularFactor.sharedPreference -> setupChange = true
        }
    }

    // Méthodes
    private fun setupRenderingMode() {
        Log.d(TAG, "rendering mode  : ${if (wireRendering) "line loop" else "triangles"}")
        polyProgram.mode = if (wireRendering) GLES31.GL_LINE_LOOP else polyProgram.defaultMode
    }

    private fun setupTransparency() {
        Log.d(TAG, "transparency    : ${if (transparency) "activated" else "disactivated"}")
        (if (transparency) this::activateTransparency else this::disactivateTransparency)()
    }

    private fun setupLightPower() {
        Log.d(TAG, "light power     : $lightPower")
        polyProgram.lightPower = lightPower.toFloat()
    }

    private fun setupColorFactors() {
        Log.d(TAG, "ambient factor  : $ambientFactor%")
        Log.d(TAG, "diffuse factor  : $diffuseFactor%")
        Log.d(TAG, "specular factor : $specularFactor%")

        // update factors
        polyProgram.ambientFactor  = ambientFactor  / 100f
        polyProgram.diffuseFactor  = diffuseFactor  / 100f
        polyProgram.specularFactor = specularFactor / 100f
    }

    private fun activateTransparency() {
        GLES31.glDisable(GLES31.GL_DEPTH_TEST)
        GLES31.glDepthMask(false)

        GLES31.glEnable(GLES31.GL_BLEND)
        GLES31.glBlendFunc(GLES31.GL_ONE, GLES31.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun disactivateTransparency() {
        GLES31.glEnable(GLES31.GL_DEPTH_TEST)
        GLES31.glDepthMask(true)

        GLES31.glDisable(GLES31.GL_BLEND)
    }
}