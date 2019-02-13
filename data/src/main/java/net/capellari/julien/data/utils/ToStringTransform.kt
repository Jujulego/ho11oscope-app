package net.capellari.julien.data.utils

import net.capellari.julien.data.Source
import net.capellari.julien.data.Transform

class ToStringTransform<T: Any>(val default: String = "") : Transform<T, String>(default) {
    // Méthodes
    override fun applyTransform(data: T, origin: Source<T>): String {
        return data.toString()
    }
}