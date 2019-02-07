package net.capellari.julien.wrapper

import android.widget.TextView

class Printer<T>(private val textView: TextView, default: T, format: String? = null): ValueHolder<T> {
    // Méthodes
    private fun refresh() {
        textView.text = format?.format(value) ?: value.toString()
    }

    // Propriétés
    override var value: T = default
        set(value) { field = value
            refresh()
        }

    var format: String? = format
        set(value) { field = value
            refresh()
        }
}