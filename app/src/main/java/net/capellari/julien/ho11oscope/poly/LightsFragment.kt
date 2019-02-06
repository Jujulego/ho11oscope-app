package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_light.view.*
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.inflate
import net.capellari.julien.wrapper.*
import kotlin.math.*

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
                PointLight(0f, 1f, 10f)
        )

        // Méthodes
        override fun getItemCount(): Int = lights.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightHolder {
            return LightHolder(parent.inflate(R.layout.poly_light, false))
        }

        override fun onBindViewHolder(holder: LightHolder, position: Int) {
            holder.bind(position + 1, lights[position])
        }
    }

    inner class LightHolder(val view: View) : RecyclerView.ViewHolder(view), ValueListener<Int> {
        // Attributs
        var light: PointLight? = null

        private val distance = NumberLinker(10).apply {
            add(SeekBarWrapper(view.seek_distance), true)
            add(NumberTextWrapper(view.edit_distance))
        }

        private val hauteur = NumberLinker(1, 10, -10).apply {
            add(SeekBarWrapper(view.seek_hauteur), NumberTextWrapper(view.edit_hauteur))
        }

        private val angle = NumberLinker(0, 180, -180).apply {
            add(SeekBarWrapper(view.seek_angle), NumberTextWrapper(view.edit_angle))
        }

        // Initialisation
        init {
            distance.addValueListener(this)
            hauteur.addValueListener(this)
            angle.addValueListener(this)
        }

        // Events
        override fun onNewValue(value: Int, from: InputWrapper<Int>) {
            updatePos()
        }

        // Méthodes
        fun bind(id: Int, value: PointLight) {
            light = value

            // Set values
            view.nom.text = getString(R.string.poly_light_pointlight, id)

            distance.value = light!!.position.xz.length.toInt()
            hauteur.value  = light!!.position.y.toInt()
            angle.value    = (tan(light!!.position.x / light!!.position.z) * 180 / Math.PI).toInt()
        }

        private fun updatePos() {
            light?.apply {
                // Get values
                val d = distance.value.toFloat()
                val h = hauteur.value.toFloat()
                val a = angle.value * Math.PI.toFloat() / 180f

                // Apply
                position = Vec3((d * sin(a)), h, (d * cos(a)))
                updateLights()
            }
        }
    }
}