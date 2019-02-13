package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

class Linker<T>(default: T) : Source<T>, SourceImpl<T>() {
    // Attributs
    private var _data: T = default
    private var sinks   = mutableSetOf<Sink<T>>()
    private var configs = mutableSetOf<Configurable>()
    private val config  = mutableMapOf<String,Any?>()

    private val internal_sink = object : Sink<T> {
        // Méthodes
        override fun updateData(data: T, origin: Source<T>) {
            // to all childs
            sinks.forEach { it.updateData(data, origin) }

            // to self and up
            _data = data
            emitData(data, origin)
        }
    }

    // Propriétés
    override var data: T get() = _data
        set(value) {
            // to all childs
            sinks.forEach { it.updateData(value, this) }

            // to self and up
            _data = value
            emitData(value, this)
        }

    // Méthodes
    private fun addConfig(config: Configurable, keep: Boolean) {
        // Attributs
        if (keep) {
            config.applyConfig(this)
        } else {
            this.applyConfig(config)
        }

        // Liste !
        configs.add(config)
    }

    fun link(noeud: Noeud<T>, keep: Boolean = false) {
        if (keep) {
            // Keep it's data
            data = noeud.data
        } else {
            // Apply current data
            noeud.updateData(data, this)
        }

        // Add !
        noeud.addSink(internal_sink)
        sinks.add(noeud)

        addConfig(noeud, keep)
    }
    fun link(sink: Sink<T>, keep: Boolean = false) {
        // Apply current data
        sink.updateData(data, this)

        // Add !
        sinks.add(sink)
        addConfig(sink, keep)
    }
    fun link(source: Source<T>, keep: Boolean = false) {
        // Keep it's data
        if (keep) {
            data = source.data
        }

        // Add !
        source.addSink(internal_sink)
        addConfig(source, keep)
    }

    fun unlink(noeud: Noeud<T>) {
        sinks.remove(noeud)
        configs.remove(noeud)
    }
    fun unlink(sink: Sink<T>) {
        sinks.remove(sink)
        configs.remove(sink)
    }
    fun unlink(source: Source<T>) {
        configs.remove(source)
    }

    // Opérateurs
    override fun get(nom: String): Any? {
        return config[nom]
    }

    override fun set(nom: String, value: Any?) {
        config[nom] = value
        configs.forEach { it[nom] = value }
    }
}