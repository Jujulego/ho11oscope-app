package net.capellari.julien.data.wrappers

import android.os.Build
import android.widget.SeekBar
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.SourceImpl
import net.capellari.julien.data.property

class SeekBarWrapper(val seekbar: SeekBar): Noeud<Int>, SourceImpl<Int>() {
    // Attributs
    private var _min: Int = 0 // compatibility

    // Propriétés
    override var data: Int get() = fromSeekBar(seekbar.progress)
        set(value) { seekbar.progress = toSeekBar(value) }

    var max: Int by property("max", 100)
    var min: Int by property("min", 0)

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
        this.data = data
    }

    override fun getKeys(): MutableSet<String> {
        val keys = super<SourceImpl>.getKeys()
        keys.add("max")
        keys.add("min")

        return keys
    }

    override fun get(nom: String): Any? {
        return when(nom) {
            "max" -> fromSeekBar(seekbar.max)
            "min" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    seekbar.min
                } else {
                    _min
                }
            }

            else -> super<SourceImpl>.get(nom)
        }
    }

    override fun set(nom: String, value: Any?) {
        when(nom) {
            "max" -> seekbar.max = toSeekBar((value as? Int) ?: 100)
            "min" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    seekbar.min = (value as? Int) ?: 100
                } else {
                    val v = (value as? Int) ?: 0

                    // Adapt !
                    max -= v - _min
                    data -= v - _min

                    // Update
                    _min = v
                }
            }

            else -> super<SourceImpl>.set(nom, value)
        }
    }
}