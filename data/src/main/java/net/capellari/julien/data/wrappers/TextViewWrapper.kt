package net.capellari.julien.data.wrappers

import android.widget.TextView
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source

class TextViewWrapper<T: Any>(val textview: TextView): Sink<T> {
    // Méthodes
    override fun updateData(data: T, origin: Source<T>) {
        textview.text = data.toString()
    }
}