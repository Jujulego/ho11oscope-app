package net.capellari.julien.data.wrappers

import android.widget.TextView
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source
import net.capellari.julien.data.property

class TextViewWrapper<T: Any>(val textview: TextView): Sink<T> {
    // Attributs
    var data: T? = null
        private set

    var format: String? by property("format", this::refresh)
    private var _format: String? = null

    // Opérateurs
    override fun get(nom: String): Any? {
        return when(nom) {
            "format" -> _format
            else -> super.get(nom)
        }
    }

    override fun set(nom: String, value: Any?) {
        if (nom == "format") {
            _format = value as? String
            refresh()
        }
    }

    // Méthodes
    override fun getKeys(): MutableSet<String> {
        return mutableSetOf("format")
    }

    private fun refresh() {
        textview.text = data?.let { format?.format(it) ?: it.toString() } ?: ""
    }

    override fun updateData(data: T, origin: Source<T>) {
        this.data = data
        refresh()
    }
}