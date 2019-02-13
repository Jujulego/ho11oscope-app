package net.capellari.julien.data.wrappers

import android.os.Build
import android.widget.SeekBar
import net.capellari.julien.data.Property
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.NoeudImpl

class SeekBarWrapper(val seekbar: SeekBar): NoeudImpl<Int>() {
    // Attributs
    private var _min: Int = 0 // compatibility

    // Propriétés
    override var data: Int get() = fromSeekBar(seekbar.progress)
        set(value) {
            seekbar.progress = toSeekBar(value)
            emitData(value)
        }

    @Property
    var max: Int get() = fromSeekBar(seekbar.max)
        set(value) { seekbar.max = toSeekBar(value) }

    @Property
    var min: Int get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) seekbar.min else _min
        set(value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                seekbar.min = value
            } else {
                // Adapt !
                max -= value - _min
                data -= value - _min

                // Update
                _min = value
            }
        }

    // Initialisation
    init {
        // Listeners
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    emitData(fromSeekBar(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    // Méthodes
    private fun fromSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) value else (value + _min)

    private fun toSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) value else (value - _min)

    override fun updateData(data: Int, origin: Source<Int>) {
        seekbar.progress = toSeekBar(data)
    }
}