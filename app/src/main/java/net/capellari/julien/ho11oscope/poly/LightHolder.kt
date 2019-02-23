package net.capellari.julien.ho11oscope.poly

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.poly_light.view.*
import net.capellari.julien.data.Linker
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source
import net.capellari.julien.data.utils.StringToIntConverter
import net.capellari.julien.data.wrappers.EditTextWrapper
import net.capellari.julien.data.wrappers.SeekBarWrapper
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.opengl.PointLight
import net.capellari.julien.opengl.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class LightHolder(val view: View, val frag: LightsFragment) : RecyclerView.ViewHolder(view),
        Sink<Int>, PopupMenu.OnMenuItemClickListener {
    // Attributs
    var light: PointLight? = null
    private var menu: PopupMenu

    private val distance = Linker(10).apply {
        link(SeekBarWrapper(view.seek_distance), keep = true)
        link(StringToIntConverter(EditTextWrapper(view.edit_distance)))
    }

    private val hauteur = Linker(1, "max" to 10, "min" to -10).apply {
        link(SeekBarWrapper(view.seek_hauteur))
        link(StringToIntConverter(EditTextWrapper(view.edit_hauteur)))
    }

    private val angle = Linker(0, "max" to 180, "min" to -180).apply {
        link(SeekBarWrapper(view.seek_angle))
        link(StringToIntConverter(EditTextWrapper(view.edit_angle)))
    }

    // Initialisation
    init {
        distance.addSink(this)
        hauteur.addSink(this)
        angle.addSink(this)

        // toggle btn
        view.btn_activate.setOnCheckedChangeListener { _, isChecked ->
            light?.apply {
                isActive = isChecked
                frag.updateLights()
            }
        }

        // Popup Menu
        menu = PopupMenu(frag.requireContext(), view.light_menu)
        menu.inflate(R.menu.menu_poly_light)

        menu.setOnMenuItemClickListener(this)

        view.light_menu.setOnClickListener {
            menu.show()
        }
    }

    // Events
    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId) {
            else -> false
        }
    }

    override fun updateData(data: Int, origin: Source<Int>) {
        updatePos()
    }

    // Méthodes
    fun bind(id: Int, value: PointLight) {
        light = null

        // Set values
        view.nom.text = frag.getString(R.string.poly_light_pointlight, id)

        distance.data = value.position.xz.length.toInt()
        hauteur.data  = value.position.y.toInt()
        angle.data    = (tan(value.position.x / value.position.z) * 180f / Math.PI).toInt()-360

        view.btn_activate.isChecked = value.isActive

        // Keep light
        light = value
    }

    private fun updatePos() {
        light?.apply {
            // Get values
            val d = distance.data.toFloat()
            val h = hauteur.data.toFloat()
            val a = angle.data * Math.PI.toFloat() / 180f

            // Apply
            position = Vec3((d * sin(a)), h, (d * cos(a)))
            frag.updateLights()
        }
    }
}