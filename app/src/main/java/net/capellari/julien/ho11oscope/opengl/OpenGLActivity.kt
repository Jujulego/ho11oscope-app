package net.capellari.julien.ho11oscope.opengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class OpenGLActivity : AppCompatActivity() {
    // Attributs
    private lateinit var glView: GLSurfaceView

    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Views
        glView = HologramSurfaceView(this)
        setContentView(glView)
    }
}