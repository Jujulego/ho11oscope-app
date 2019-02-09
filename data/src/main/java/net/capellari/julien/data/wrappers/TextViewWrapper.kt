package net.capellari.julien.data.wrappers

import android.widget.TextView
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source
import net.capellari.julien.data.base.SinkImpl
import net.capellari.julien.data.property

class TextViewWrapper<T: Any>(val textview: TextView): Sink<T>, SinkImpl<T>() {
    // Attributs
    var string: String = ""
        private set

    var format: String? by property("format", this::refresh)

    // Méthodes
    private fun refresh() {
        textview.text = format?.format(string) ?: string
    }

    override fun updateData(data: T, origin: Source<T>) {
        string = data.toString()
        refresh()
    }
}