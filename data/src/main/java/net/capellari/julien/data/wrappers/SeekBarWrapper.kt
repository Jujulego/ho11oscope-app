package net.capellari.julien.data.wrappers

import android.widget.SeekBar
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.SourceImpl

class SeekBarWrapper(val seekbar: SeekBar): Noeud<Int>, SourceImpl<Int>() {
    // Propriétés
    override val data: Int get() = seekbar.progress

    // Initialisation
    init {
        // Listeners
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    emitData(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    // Méthodes
    override fun updateData(data: Int, origin: Source<Int>) {
        seekbar.progress = data
    }
}