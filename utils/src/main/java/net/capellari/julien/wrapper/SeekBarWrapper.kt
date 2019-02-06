package net.capellari.julien.wrapper

import android.os.Build
import android.widget.SeekBar

class SeekBarWrapper(val seekBar: SeekBar) : BaseInputWrapper<Int>(), NumberWrapper<Int> {
    // Attribute
    private var _min: Int = 0 // used only for compatibility

    // Initialisation
    init {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) emit(fromSeekBar(progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Constructeurs
    constructor(seekBar: SeekBar, max: Int, min: Int = 0): this(seekBar) {
        this.max = max
        this.min = min
    }

    // Méthodes
    private fun fromSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= 30) value else (value + _min)

    private fun toSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= 30) value else (value - _min)

    // Propriétés
    override var max: Int
        get() = fromSeekBar(seekBar.max)
        set(value) { seekBar.max = toSeekBar(value) }

    override var min: Int get() = if (Build.VERSION.SDK_INT >= 30) seekBar.min else _min
        set(value) {
            if (Build.VERSION.SDK_INT >= 30) {
                seekBar.min = value
            } else {
                // Adapt !
                max -= value - _min
                this.value -= value - _min

                // Update
                _min = value
            }
        }

    override var value: Int
        get() = fromSeekBar(seekBar.progress)
        set(value) { seekBar.progress = toSeekBar(value) }
}