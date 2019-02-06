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
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.utils.inflate
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

    inner class LightHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Attributs
        var light: PointLight? = null
        var noUpdate = false

        // Propriétés
        var distance: Float = 10f
            private set(v) {
                field = v
                if (!noUpdate) updatePos()
            }

        var hauteur: Float = 1f
            private set(v) {
                field = v
                if (!noUpdate) updatePos()
            }

        var angle: Float = 0f
            private set(v) {
                field = v
                if (!noUpdate) updatePos()
            }

        // Initialisation
        init {
            // Listeners
            view.seek_distance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        distance = progress.toFloat()
                        view.edit_distance.setText(progress.toString())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            view.edit_distance.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = txt.toString().toIntOrNull() ?: 0
                    nval = max(nval, 0)
                    nval = min(nval, view.seek_distance.max)

                    view.seek_distance.progress = nval
                    distance = nval.toFloat()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            view.seek_hauteur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        hauteur = progress.toFloat() - 10
                        view.edit_hauteur.setText((progress - 10).toString())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            view.edit_hauteur.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = (txt.toString().toIntOrNull() ?: 0) + 10
                    nval = max(nval, 0)
                    nval = min(nval, view.seek_hauteur.max)

                    view.seek_hauteur.progress = nval
                    hauteur = (nval - 10).toFloat()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            view.seek_angle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        angle = (progress * Math.PI / 180f).toFloat()
                        view.edit_angle.setText(progress.toString())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            view.edit_angle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = txt.toString().toIntOrNull() ?: 0
                    nval = max(nval, 0)
                    nval = min(nval, view.seek_angle.max)

                    view.seek_angle.progress = nval
                    angle = (nval * Math.PI / 180f).toFloat()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Méthodes
        fun bind(id: Int, value: PointLight) {
            light = value

            // Récupérer les coordonnées
            noUpdate = true
                distance = light!!.position.xz.length
                hauteur  = light!!.position.y
                angle    = tan(light!!.position.x / light!!.position.z) + Math.PI.toFloat()
            noUpdate = false

            // Set values
            view.nom.text = getString(R.string.poly_light_pointlight, id)

            distance.toInt().let {
                view.seek_distance.progress = it
                view.edit_distance.setText(it.toString())
            }

            hauteur.toInt().let {
                view.seek_hauteur.progress = it + 10
                view.edit_hauteur.setText(it.toString())
            }

            (angle * 180 / Math.PI).toInt().let {
                view.seek_angle.progress = it
                view.edit_angle.setText(it.toString())
            }
        }

        private fun updatePos() {
            light?.apply {
                position = Vec3((distance * -sin(angle)), hauteur, (distance * -cos(angle)))
                updateLights()
            }
        }
    }
}