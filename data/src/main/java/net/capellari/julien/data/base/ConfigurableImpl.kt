package net.capellari.julien.data.base

import net.capellari.julien.data.Configurable

abstract class ConfigurableImpl: Configurable {
    // Attributs
    override val attributs = mutableMapOf<String,Any?>()
}