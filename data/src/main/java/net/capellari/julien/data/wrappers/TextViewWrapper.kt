package net.capellari.julien.data.wrappers

import android.widget.TextView
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.ConfigurableImpl
import net.capellari.julien.data.base.SinkImpl
import net.capellari.julien.data.property

class TextViewWrapper<T: Any>(val textview: TextView): SinkImpl<T>() {
    // Attributs
    var data: T? = null
        private set

    var format: String? by property("format", this::refresh)

    // Méthodes
    override fun getKeys(): MutableSet<String> {
        return super.getKeys().apply { add("format") }
    }

    private fun refresh() {
        textview.text = data?.let { format?.format(it) ?: it.toString() } ?: ""
    }

    override fun updateData(data: T, origin: Source<T>) {
        this.data = data
        refresh()
    }
}