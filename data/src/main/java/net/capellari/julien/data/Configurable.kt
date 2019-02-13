package net.capellari.julien.data

import androidx.annotation.CallSuper
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

interface Configurable {
    // Méthodes
    fun applyConfig(target: Configurable) {
        getKeys().forEach { key -> target.setProp(key, this.getProp<Any>(key)) }
    }

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