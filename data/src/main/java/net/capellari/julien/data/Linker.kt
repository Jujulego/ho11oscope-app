package net.capellari.julien.data

import net.capellari.julien.data.base.SinkImpl
import net.capellari.julien.data.base.SourceImpl

class Linker<T>(default: T) : Source<T>, SourceImpl<T>() {
    // Attributs
    private var _data: T = default
    private var sinks   = mutableSetOf<Sink<T>>()
    private var configs = mutableSetOf<Configurable>()

    private val internal_sink = object : SinkImpl<T>() {
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
        noeud.addSink(internal_sink)
        sinks.add(noeud)

        addConfig(noeud, keep)
    }
    fun link(sink: Sink<T>, keep: Boolean = false) {
        sinks.add(sink)
        addConfig(sink, keep)
    }
    fun link(source: Source<T>, keep: Boolean = false) {
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
    override fun set(nom: String, value: Any?) {
        super<SourceImpl>.set(nom, value)
        configs.forEach { it[nom] = value }
    }
}