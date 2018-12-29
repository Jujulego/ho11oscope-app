package net.capellari.julien.ho11oscope.poly

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.capellari.julien.fragments.ListFragment
import net.capellari.julien.ho11oscope.R
import net.capellari.julien.utils.inflate

class LightsFragment : ListFragment() {
    // Attributs
    val adapter = LightAdapter()

    // Méthodes
    override fun onRecyclerViewCreated(view: RecyclerView) {
        view.adapter = adapter
        view.layoutManager = LinearLayoutManager(context)
    }

    // Classes
    inner class LightAdapter : RecyclerView.Adapter<LightHolder>() {
        // Attributs
        val lights = arrayOf(
                Light(10f, 45f, 260),
                Light(10f, 45f, 260)
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
        val light: Light? = null

        // Méthodes
        fun bind(value: Light) {

        }
    }
}