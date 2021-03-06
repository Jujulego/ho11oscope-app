package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.preference.PreferenceManager
import net.capellari.julien.opengl.AssimpMesh
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.sharedPreference
import java.lang.Math.abs
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sign

class HologramRenderer(val context: Context): GLSurfaceView.Renderer {
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
        val EYE =    Vec3(0f, 0f, -3f)
        val TARGET = Vec3(0f, 0f,  0f)
        val UP =     Vec3(0f, 1f,  0f)
    }

    // Attributs
    private var polyProgram:      PolyProgram      = PolyProgram.instance
    private var normalsProgram:   NormalsProgram   = NormalsProgram.instance
    private var wireframeProgram: WireframeProgram = WireframeProgram.instance

    private var readyToRender = false

    private var lastFrameTime: Long = 0
    private var angle: Float = 0f // current rotation angle (deg)

    private val scale = Mat4.scale(.45f, .45f, .45f)
    private val positions = arrayOf(
            Mat4.rotate( 90f, 0f, 0f, 1f),
            Mat4.rotate(180f, 0f, 0f, 1f) * Mat4.rotate(180f, 0f, 1f, 0f),
            Mat4.rotate( 90f, 0f, 0f, 1f),
            Mat4.rotate(  0f, 0f, 0f, 1f)
    )
    private val eyes = arrayOf(
            Vec3( 0f, 0f, -3f),
            Vec3(-3f, 0f,  0f),
            Vec3( 0f, 0f,  3f),
            Vec3( 3f, 0f,  0f)
    )
    private val targets = arrayOf(
            Vec3( 1f,  0f, 0f),
            Vec3( 0f,  1f, 0f),
            Vec3( 1f,  0f, 0f),
            Vec3( 0f, -1f, 0f)
    )

    @Volatile
    var asset: Asset? = null // object to render
        set(asset) {
            field = asset
            readyToRender = false
            Log.d(TAG, "Recieved new object to render")
        }
    private var meshes = arrayListOf<AssimpMesh>()

    // Propriétés
    private var transparency       by sharedPreference("transparency",        context, false)
    private var wireframeRendering by sharedPreference("wireframe_rendering", context, false)
    private var normalsRendering   by sharedPreference("normals_rendering",   context, false)

    private var ambientFactor  by sharedPreference("ambientFactor",  context, 50)
    private var diffuseFactor  by sharedPreference("diffuseFactor",  context, 50)
    private var specularFactor by sharedPreference("specularFactor", context, 50)

    private var magnitude  by sharedPreference("explodeMagnitude", context, 50)

    // Méthodes
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        GLES32.glClearColor(0f, 0f, 0f, 1f)

        lastFrameTime = System.currentTimeMillis()
        polyProgram.compile(context)
        normalsProgram.compile(context)
        wireframeProgram.compile(context)

        // Setup
        setupColorFactors()
        setupTransparency()
    }

    override fun onDrawFrame(unused: GL10?) {
        // Update spin animation
        val now    = System.currentTimeMillis()
        val deltaT = minOf((now - lastFrameTime) * 0.001f, 0.1f)

        lastFrameTime = now
        angle += deltaT * MODEL_ROTATION_SPEED

        if (angle >= 360) angle -= 360

        // Draw background
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        val m = magnitude / 20f
        if (polyProgram.magnitude != m) {
            polyProgram.magnitude += (m - polyProgram.magnitude) * deltaT

            if (polyProgram.magnitude < m) {
                polyProgram.magnitude = minOf(m, polyProgram.magnitude)
            } else {
                polyProgram.magnitude = maxOf(m, polyProgram.magnitude)
            }

            wireframeProgram.magnitude = polyProgram.magnitude
        }

        // render
        if (readyToRender) {
            for (i in 0 until 4) {
                // Compute MVP Matrix
                polyProgram.matrices.lightMatrix = positions[i]
                polyProgram.matrices.modelMatrix = positions[i] * Mat4.rotate(angle, 0f, 1f, 0f) * scale
                polyProgram.stables.viewMatrix = Mat4.lookAt(eyes[i], targets[i], HologramRenderer.UP)
                polyProgram.matrices.mvpMatrix = polyProgram.stables.projMatrix * (polyProgram.stables.viewMatrix * polyProgram.matrices.modelMatrix)

                if (wireframeRendering) {
                    wireframeProgram.render(meshes)
                } else {
                    polyProgram.render(meshes)
                }

                if (normalsRendering) {
                    normalsProgram.render(meshes)
                }
            }

        } else {
            asset?.also {
                meshes.clear()

                for (mesh in it.model.meshes) {
                    meshes.add(AssimpMesh(mesh))
                }

                polyProgram.prepare(meshes)

                readyToRender = true
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        polyProgram.stables.projMatrix = Mat4.perspective(FOV_Y, ratio, NEAR_CLIP, FAR_CLIP)
    }

    // Méthodes
    private fun setupTransparency() {
        Log.d(TAG, "transparency    : ${if (transparency) "activated" else "disactivated"}")

        if (transparency) activateTransparency() else disactivateTransparency()
    }

    private fun setupColorFactors() {
        Log.d(TAG, "ambient factor  : $ambientFactor%")
        Log.d(TAG, "diffuse factor  : $diffuseFactor%")
        Log.d(TAG, "specular factor : $specularFactor%")

        // update factors
        polyProgram.light.ambient  = ambientFactor  / 100f
        polyProgram.light.diffuse  = diffuseFactor  / 100f
        polyProgram.light.specular = specularFactor / 100f
    }

    private fun activateTransparency() {
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
        GLES32.glDepthMask(false)

        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun disactivateTransparency() {
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glDepthMask(true)

        GLES32.glDisable(GLES32.GL_BLEND)
    }
}