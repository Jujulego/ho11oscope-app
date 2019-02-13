package net.capellari.julien.data.utils

import net.capellari.julien.data.Converter
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.Property

class StringToIntConverter(noeud: Noeud<String>)
    : Converter<String,Int>(StringToIntTransform(), ToStringTransform(), noeud) {

    // Propriétés
    @Property
    var max: Int? get() = f2t.getProp("max")
        set(value) { f2t.setProp("max", value) }

    @Property
    var min: Int? get() = f2t.getProp("min")
        set(value) { f2t.setProp("min", value) }

    // Opérateurs
    /*override fun get(nom: String): Any? {
        return when(nom) {
            "max", "min" -> f2t[nom]
            else -> super.get(nom)
        }
    }

    override fun set(nom: String, value: Any?) {
        when(nom) {
            "max", "min" -> f2t[nom] = value
        }
    }*/

    // Méthodes
    /*override fun getKeys(): MutableSet<String> {
        return mutableSetOf("max", "min")
    }*/
}