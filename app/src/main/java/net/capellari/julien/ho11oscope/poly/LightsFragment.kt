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
import net.capellari.julien.wrapper.InputWrapper
import net.capellari.julien.wrapper.SeekbarWrapper
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

        private val _distance = SeekbarWrapper(view.seek_distance)
        private val _hauteur  = SeekbarWrapper(view.seek_hauteur, 10, -10)
        private val _angle    = SeekbarWrapper(view.seek_angle, 180, -180)

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
            _distance.addValueListener(object : InputWrapper.OnValueChanged<Int> {
                override fun onValueChanged(value: Int) {
                    distance = value.toFloat()
                    view.edit_distance.setText(value.toString())
                }
            })
            view.edit_distance.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = txt.toString().toIntOrNull() ?: 0
                    nval = max(nval, _distance.min)
                    nval = min(nval, _distance.max)

                    _distance.value = nval
                    distance = nval.toFloat()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            _hauteur.addValueListener(object : InputWrapper.OnValueChanged<Int> {
                override fun onValueChanged(value: Int) {
                    hauteur = value.toFloat()
                    view.edit_hauteur.setText(value.toString())
                }
            })
            view.edit_hauteur.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = txt.toString().toIntOrNull() ?: 0
                    nval = max(nval, _hauteur.min)
                    nval = min(nval, _hauteur.max)

                    _hauteur.value = nval
                    hauteur = nval.toFloat()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            _angle.addValueListener(object : InputWrapper.OnValueChanged<Int> {
                override fun onValueChanged(value: Int) {
                    angle = (value * Math.PI / 180f).toFloat()
                    view.edit_angle.setText(value.toString())
                }
            })
            view.edit_angle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(txt: Editable) {
                    var nval = txt.toString().toIntOrNull() ?: 0
                    nval = max(nval, _angle.min)
                    nval = min(nval, _angle.max)

                    _angle.value = nval
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
                angle    = tan(light!!.position.x / light!!.position.z)
            noUpdate = false

            // Set values
            view.nom.text = getString(R.string.poly_light_pointlight, id)

            distance.toInt().let {
                _distance.value = it
                view.edit_distance.setText(it.toString())
            }

            hauteur.toInt().let {
                _hauteur.value = it
                view.edit_hauteur.setText(it.toString())
            }

            (angle * 180 / Math.PI).toInt().let {
                _angle.value = it
                view.edit_angle.setText(it.toString())
            }
        }

        private fun updatePos() {
            light?.apply {
                position = Vec3((distance * sin(angle)), hauteur, (distance * cos(angle)))
                updateLights()
            }
        }
    }
}