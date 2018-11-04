package net.capellari.julien.ho11oscope.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class HologramSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    // Companion
    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = -180.0f / 320f
    }

    // Attributs
    private val renderer: HologramRenderer

    private var prevX: Float = 0f
    private var prevY: Float = 0f

    // Constructeur
    init {
        // Config
        setEGLContextClientVersion(2)

        // Renderer
        renderer = HologramRenderer(context)
        setRenderer(renderer)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
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

                renderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR
                requestRender()
            }
        }

        prevX = x
        prevY = y

        return true
    }
}