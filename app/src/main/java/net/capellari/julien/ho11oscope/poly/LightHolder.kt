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
            R.id.action_deactivate -> {
                item.isChecked = !item.isChecked

                light?.apply {
                    isActive = !item.isChecked
                    frag.updateLights()
                }

                true
            }

            else -> false
        }
    }

    override fun updateData(data: Int, origin: Source<Int>) {
        updatePos()
    }

    // Méthodes
    fun bind(id: Int, value: PointLight) {
        light = value

        // Set values
        view.nom.text = frag.getString(R.string.poly_light_pointlight, id)

        distance.data = light!!.position.xz.length.toInt()
        hauteur.data  = light!!.position.y.toInt()
        angle.data    = (tan(light!!.position.x / light!!.position.z) * 180 / Math.PI).toInt()

        menu.menu.findItem(R.id.action_deactivate)?.isChecked = !light!!.isActive
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