package net.capellari.julien.data.utils

import net.capellari.julien.data.Source
import net.capellari.julien.data.Transform
import net.capellari.julien.data.property
import kotlin.math.max
import kotlin.math.min

class StringToIntTransform(val default: Int = 0) : Transform<String,Int>(default) {
    // Propriétés
    var max: Int by property("max", Int.MAX_VALUE)
    var min: Int by property("min", Int.MIN_VALUE)

    // Méthodes
    override fun applyTransform(data: String, origin: Source<String>): Int {
        return min(max(data.toIntOrNull() ?: default, min), max)
    }

    override fun getKeys(): MutableSet<String> {
        return super.getKeys().apply { addAll(listOf("max", "min")) }
    }
}