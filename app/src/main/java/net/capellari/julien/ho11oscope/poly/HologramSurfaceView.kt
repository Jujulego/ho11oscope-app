package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class HologramSurfaceView : GLSurfaceView {
    // Attributs
    var renderer: HologramRenderer
        private set

    // Constructeurs
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        // setup
        setEGLContextClientVersion(3)

        // renderer
        renderer = HologramRenderer(context)
        setRenderer(renderer)
    }
}