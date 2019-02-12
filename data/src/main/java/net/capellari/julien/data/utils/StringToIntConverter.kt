package net.capellari.julien.data.utils

import net.capellari.julien.data.Converter
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.property

class StringToIntConverter(noeud: Noeud<String>)
    : Converter<String,Int>(StringToIntTransform(), ToStringTransform(), noeud) {

    // Propriétés
    var max: Int by property("max", Int.MAX_VALUE)
    var min: Int by property("min", Int.MIN_VALUE)

    // Opérateurs
    override fun get(nom: String): Any? {
        return when(nom) {
            "max", "min" -> f2t[nom]
            else -> super.get(nom)
        }
    }

    override fun set(nom: String, value: Any?) {
        when(nom) {
            "max", "min" -> f2t[nom] = value
            else -> super.set(nom, value)
        }
    }

    // Méthodes
    override fun getKeys(): MutableSet<String> {
        return super.getKeys().apply { addAll(listOf("max", "min")) }
    }
}