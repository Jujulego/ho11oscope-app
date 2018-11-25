package net.capellari.julien.ho11oscope.opengl

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.opengl_fragment.view.*
import net.capellari.julien.ho11oscope.R

class OpenGLFragment : Fragment() {
    // Events
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.opengl_fragment, container, false)
        view.hologram.setupTriangle()

        // Events
        view.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    resources.getText(R.string.opengl_triangle) -> view.hologram.setupTriangle()
                    resources.getText(R.string.opengl_carre)    -> view.hologram.setupCarre()
                    resources.getText(R.string.opengl_hexagone) -> view.hologram.setupHexagone()
                }
            }
        })

        savedInstanceState?.let {
            view.tabs.getTabAt(it.getInt("tabIndex"))?.select()
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        view?.let { view ->
            outState.putInt("tabIndex", view.tabs.selectedTabPosition)
        }
    }
}