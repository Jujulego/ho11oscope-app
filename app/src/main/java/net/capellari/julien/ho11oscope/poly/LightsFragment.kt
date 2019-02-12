package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_light.view.*
import net.capellari.julien.data.Linker
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source
import net.capellari.julien.data.utils.StringToIntConverter
import net.capellari.julien.data.wrappers.EditTextWrapper
import net.capellari.julien.data.wrappers.SeekBarWrapper
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.inflate
import net.capellari.julien.wrapper.InputWrapper
import net.capellari.julien.wrapper.ValueListener
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

    inner class LightHolder(val view: View) : RecyclerView.ViewHolder(view), ValueListener<Int>, Sink<Int> {
        // Attributs
        var light: PointLight? = null

        override val attributs = mutableMapOf<String, Any?>()

        private val distance = Linker(10).apply {
            link(SeekBarWrapper(view.seek_distance), keep = true)
            link(StringToIntConverter(EditTextWrapper(view.edit_distance)))
        }

        private val hauteur = Linker(1).apply {
            link(SeekBarWrapper(view.seek_hauteur))
            link(StringToIntConverter(EditTextWrapper(view.edit_hauteur)))

            this["max"] = 10
            this["min"] = -10
        }

        private val angle = Linker(0).apply {
            link(SeekBarWrapper(view.seek_angle))
            link(StringToIntConverter(EditTextWrapper(view.edit_angle)))

            this["max"] = 180
            this["min"] = -180
        }

        // Initialisation
        init {
            distance.addSink(this)
            hauteur.addSink(this)
            angle.addSink(this)
        }

        // Events
        override fun onNewValue(value: Int, from: InputWrapper<Int>) {
            updatePos()
        }

        override fun updateData(data: Int, origin: Source<Int>) {
            updatePos()
        }

        // Méthodes
        fun bind(id: Int, value: PointLight) {
            light = value

            // Set values
            view.nom.text = getString(R.string.poly_light_pointlight, id)

            distance.data = light!!.position.xz.length.toInt()
            hauteur.data  = light!!.position.y.toInt()
            angle.data    = (tan(light!!.position.x / light!!.position.z) * 180 / Math.PI).toInt()
        }

        private fun updatePos() {
            light?.apply {
                // Get values
                val d = distance.data.toFloat()
                val h = hauteur.data.toFloat()
                val a = angle.data * Math.PI.toFloat() / 180f

                // Apply
                position = Vec3((d * sin(a)), h, (d * cos(a)))
                updateLights()
            }
        }
    }
}