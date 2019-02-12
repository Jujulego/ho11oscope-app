package net.capellari.julien.data

import kotlin.reflect.KProperty

interface Configurable {
    // Attributs
    val attributs: MutableMap<String,Any?>

    // Méthodes
    fun getKeys(): MutableSet<String> {
        val keys = mutableSetOf<String>()
        keys.addAll(attributs.keys)

        return keys
    }
    fun applyConfig(target: Configurable) {
        getKeys().forEach { key -> target[key] = this[key] }
    }

    // Opérateurs
    operator fun get(nom: String): Any? = attributs[nom]
    operator fun set(nom: String, value: Any?) { attributs[nom] = value }
}

// Delegate
fun<T: Any> property(nom: String) = PropertyDelegate<T>(nom) {}
fun<T: Any> property(nom: String, default: T) = DefaultPropertyDelegate(nom, default) {}
fun<T: Any> property(nom: String, callback: () -> Unit) = PropertyDelegate<T>(nom, callback)
fun<T: Any> property(nom: String, default: T, callback: () -> Unit) = DefaultPropertyDelegate(nom, default, callback)

open class PropertyDelegate<T: Any>(val nom : String, val callback: () -> Unit) {
    // Opérateurs
    @Suppress("UNCHECKED_CAST")
    open operator fun<R: Configurable> getValue(thisRef: R, property: KProperty<*>): T? {
        return thisRef[nom] as? T
    }

    open operator fun<R : Configurable> setValue(thisRef: R, property: KProperty<*>, value: T?) {
        thisRef[nom] = value
        callback()
    }
}

class DefaultPropertyDelegate<T: Any>(nom : String, val default: T, callback: () -> Unit): PropertyDelegate<T>(nom, callback) {
    // Opérateurs
    override operator fun<R: Configurable> getValue(thisRef: R, property: KProperty<*>): T {
        return super.getValue(thisRef, property) ?: default
    }
}