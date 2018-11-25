package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class HologramPlayerFragment : Fragment() {
    // Attributs
    private lateinit var polies: PolyViewModel
    private lateinit var surface: HologramSurfaceView

    // Méthodes
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // ViewModels
        polies = ViewModelProviders.of(activity!!)[PolyViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        surface = HologramSurfaceView(context)
        polies.getAsset().observe(this, Observer<Asset> {
            surface.renderer.asset = it
        })

        return surface
    }
}