package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class OpenGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    // Companion
    companion object {
        // Constantes
        private const val TOUCH_SCALE_FACTOR: Float = -180.0f / 20f
    }

    // Attributs
    val renderer: OpenGLRenderer

    private var prevX: Float = 0f
    private var prevY: Float = 0f

    // Constructeur
    init {
        // Config
        setEGLContextClientVersion(2)

        // Renderer
        renderer = OpenGLRenderer(context)
        setRenderer(renderer)
    }

    // Méthodes
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float = event.x
        val y: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx: Float = x - prevX
                var dy: Float = y - prevY

                if (y > height / 2) {
                    dx *= -1
                }

                if (x < width / 2) {
                    dy *= -1
                }

                renderer.vit_angle = (dx + dy) * TOUCH_SCALE_FACTOR + OpenGLRenderer.VITESSE_MIN
            }
        }

        prevX = x
        prevY = y

        return true
    }

    fun setupTriangle() {
        renderer.mesh = Triangle()
        requestRender()
    }
    fun setupCarre() {
        renderer.mesh = Carre()
        requestRender()
    }
    fun setupHexagone() {
        renderer.mesh = Hexagone()
        requestRender()
    }
}