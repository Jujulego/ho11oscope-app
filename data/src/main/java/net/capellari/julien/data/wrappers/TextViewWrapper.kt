package net.capellari.julien.data.wrappers

import android.widget.TextView
import net.capellari.julien.data.Property
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source

class TextViewWrapper<T: Any>(val textview: TextView): Sink<T> {
    // Attributs
    var data: T? = null
        private set

    @Property
    var format: String? = null
        set(value) { field = value; refresh() }

    // Méthodes
    private fun refresh() {
        textview.text = data?.let { format?.format(it) ?: it.toString() } ?: ""
    }

    override fun updateData(data: T, origin: Source<T>) {
        this.data = data
        refresh()
    }
}