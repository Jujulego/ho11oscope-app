package net.capellari.julien.data

import androidx.annotation.CallSuper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

interface Configurable {
    // Méthodes
    @CallSuper
    fun getKeys(): MutableSet<String> {
        val set = mutableSetOf<String>()

        this::class.memberProperties
                .filter { it.annotations.filterIsInstance<Property>().isNotEmpty() }
                .forEach { set.add(it.name) }

        return set
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    fun<T: Any> getProp(nom: String): T? {
        val prop = this::class.memberProperties.find { it.name == nom }
        prop?.let {
            if (it.findAnnotation<Property>() != null) {
                return it.getter.call(this) as? T
            }
        }

        return null
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    fun<T: Any> setProp(nom: String, value: T?) {
        val prop = this::class.memberProperties.find { it.name == nom }
        prop?.let {
            if (it is KMutableProperty<*> && it.findAnnotation<Property>() != null) {
                it.setter.call(this, value)
            }
        }
    }
}

// Extensions
operator fun<T: Any> Configurable.get(nom: String) = getProp<T>(nom)
operator fun<T: Any> Configurable.set(nom: String, value: T?) = setProp(nom, value)

fun Configurable.applyTo(target: Configurable) {
    getKeys().forEach { key -> target.setProp(key, this.getProp<Any>(key)) }
}

// Delegate
fun<T: Any> linkTo(config: Configurable, nom: String? = null) = LinkToDelegate<T>(config, nom)

class LinkToDelegate<T: Any>(val config: Configurable, val nom: String? = null): ReadWriteProperty<Configurable,T?> {
    override fun getValue(thisRef: Configurable, property: KProperty<*>): T? {
        return config.getProp(nom ?: property.name)
    }

    override fun setValue(thisRef: Configurable, property: KProperty<*>, value: T?) {
        config.setProp(nom ?: property.name, value)
    }
}