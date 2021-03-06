package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class PolySurfaceView : GLSurfaceView {
    // Attributs
    var renderer: PolyRenderer
        private set

    // Constructeurs
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        // setup
        setEGLContextClientVersion(3)

        // renderer
        renderer = PolyRenderer(context)
        setRenderer(renderer)
    }
}