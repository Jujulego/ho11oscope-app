package net.capellari.julien.data.utils

import net.capellari.julien.data.Property
import net.capellari.julien.data.Source
import net.capellari.julien.data.Transform
import kotlin.math.max
import kotlin.math.min

class StringToIntTransform(val default: Int = 0) : Transform<String,Int>(default) {
    // Propriétés
    @Property
    var max: Int = Int.MAX_VALUE

    @Property
    var min: Int = Int.MIN_VALUE

    // Méthodes
    override fun applyTransform(data: String, origin: Source<String>): Int {
        return min(max(data.toIntOrNull() ?: default, min), max)
    }
}