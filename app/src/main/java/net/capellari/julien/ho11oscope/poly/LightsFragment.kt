package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_light.view.*
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
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
                Light(10f, 120f, 260)
        )

        // Méthodes
        override fun getItemCount(): Int = lights.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightHolder {
            return LightHolder(parent.inflate(R.layout.poly_light, false))
        }

        override fun onBindViewHolder(holder: LightHolder, position: Int) {
            holder.bind(lights[position])
        }
    }

    inner class LightHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Attributs
        var light: Light? = null

        // Initialisation
        init {
            // Listeners
            view.distance.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    light?.let {
                        val nv = s.toString().toFloatOrNull()

                        if (nv != null && it.distance != nv) {
                            it.distance = nv
                            updateLights()
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            view.power.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    light?.let {
                        val nv = s.toString().toIntOrNull()

                        if (nv != null && it.puissance != nv) {
                            it.puissance = nv
                            updateLights()
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            view.angle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        light?.let {
                            it.angle = (progress * Math.PI / 180f).toFloat()
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Méthodes
        fun bind(value: Light) {
            light = value

            // Set values
            view.distance.setText(value.distance.toString())
            view.power.setText(value.puissance.toString())
            view.angle.progress = value.angle.toInt()
        }
    }
}