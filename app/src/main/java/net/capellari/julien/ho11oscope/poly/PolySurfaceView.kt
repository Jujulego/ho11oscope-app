package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class PolySurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    // Attributs
    var renderer: PolyRenderer
        private set

    // Constructeurs
    init {
        // setup
        setEGLContextClientVersion(2)

        // renderer
        renderer = PolyRenderer(context)
        setRenderer(renderer)
    }
}