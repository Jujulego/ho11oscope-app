package net.capellari.julien.data.wrappers

import android.widget.ToggleButton
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.NoeudImpl

class ToggleButtonWrapper(val toggleButton: ToggleButton): NoeudImpl<Boolean>() {
    // Propriétés
    override var data: Boolean get() = toggleButton.isChecked
        set(v) { toggleButton.isChecked = v }

    // Initialisation
    init {
        // Listener
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            emitData(isChecked)
        }
    }

    // Méthodes
    override fun updateData(data: Boolean, origin: Source<Boolean>) {
        this.data = data
    }
}