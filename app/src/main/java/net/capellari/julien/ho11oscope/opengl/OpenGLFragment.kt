package net.capellari.julien.ho11oscope.opengl

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.opengl_fragment.view.*
import net.capellari.julien.ho11oscope.R

class OpenGLFragment : Fragment() {
    // Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.opengl_fragment, container, false)
        view.hologram.renderer.setupTriangle()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu
        inflater.inflate(R.menu.toolbar_opengl, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.tool_triangle -> {
                view?.hologram?.setupTriangle()
                item.isChecked = true
                true
            }
            R.id.tool_carre -> {
                view?.hologram?.setupCarre()
                item.isChecked = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}