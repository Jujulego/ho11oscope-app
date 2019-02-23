package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.utils.inflate

class LightsFragment : ListFragment() {
    // Attributs
    private lateinit var polyModel: PolyViewModel
    private lateinit var adapter: LightsAdapter

    // Méthodes
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // ViewModel
        polyModel = ViewModelProviders.of(activity!!)[PolyViewModel::class.java]

        adapter = LightsAdapter()
        updateLights()
    }

    override fun onRecyclerViewCreated(view: RecyclerView) {
        view.adapter = adapter
        view.layoutManager = LinearLayoutManager(context)
    }

    fun updateLights() {
        polyModel.lights.value = adapter.lights
    }

    // Classes
    inner class LightsAdapter : RecyclerView.Adapter<LightHolder>() {
        // Attributs
        var lights = arrayListOf(
                PointLight(0f, 1f, 10f),
                PointLight(5f, 2f, 5f)
        )

        // Méthodes
        override fun getItemCount(): Int = lights.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightHolder {
            return LightHolder(parent.inflate(R.layout.poly_light, false), this@LightsFragment)
        }

        override fun onBindViewHolder(holder: LightHolder, position: Int) {
            holder.bind(position + 1, lights[position])
        }
    }

}