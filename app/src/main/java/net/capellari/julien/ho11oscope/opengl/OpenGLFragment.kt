package net.capellari.julien.ho11oscope.opengl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.capellari.julien.ho11oscope.R

class OpenGLActivity : Fragment() {
    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.opengl_fragment, container, false)
    }
}