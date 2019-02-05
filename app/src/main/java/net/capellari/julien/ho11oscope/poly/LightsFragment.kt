package net.capellari.julien.ho11oscope.poly

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_light.view.*
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.inflate
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

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
            holder.bind(lights[position])
        }
    }

    inner class LightHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Attributs
        var light: PointLight? = null

        // Propriétés
        var distance: Float = 10f
            private set(v) {
                field = v
                updatePos()
            }

        var hauteur: Float = 1f
            private set(v) {
                field = v
                updatePos()
            }

        var angle: Float = 0f
            private set(v) {
                field = v
                updatePos()
            }

        // Initialisation
        init {
            // Listeners
            view.distance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) distance = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            view.hauteur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) hauteur = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            view.angle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) angle = (progress * Math.PI / 180f).toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Méthodes
        fun bind(value: PointLight) {
            light = value

            // Set values
            view.distance.progress = distance.toInt()
            view.hauteur.progress  = hauteur.toInt()
            view.angle.progress    = (angle * 180 / Math.PI).toInt()
        }

        private fun updatePos() {
            light?.apply {
                position = Vec3((distance * -sin(angle)), hauteur, (distance * -cos(angle)))
                updateLights()
            }
        }
    }
}