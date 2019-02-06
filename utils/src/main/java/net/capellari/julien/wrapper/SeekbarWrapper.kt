package net.capellari.julien.wrapper

import android.os.Build
import android.widget.SeekBar

class SeekbarWrapper(val seekBar: SeekBar) : InputWrapper<Int>(), SeekBar.OnSeekBarChangeListener {
    // Propriétés
    var max: Int
        get() = fromSeekBar(seekBar.max)
        set(value) { seekBar.max = toSeekBar(value) }

    var min: Int get() = if (Build.VERSION.SDK_INT >= 30) seekBar.min else _min
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

    private var _min: Int = 0

    override var value: Int
        get() = fromSeekBar(seekBar.progress)
        set(value) { seekBar.progress = toSeekBar(value) }

    // Initialisation
    init {
        seekBar.setOnSeekBarChangeListener(this)
    }

    constructor(seekBar: SeekBar, max: Int, min: Int = 0): this(seekBar) {
        this.max = max
        this.min = min
    }

    // Events
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) emit(fromSeekBar(progress))
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    // Méthodes
    private fun fromSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= 30) value else (value + _min)

    private fun toSeekBar(value: Int)
            = if (Build.VERSION.SDK_INT >= 30) value else (value - _min)
}