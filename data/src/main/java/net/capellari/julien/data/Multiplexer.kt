package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

open class Multiplexer<T>(default: T): Source<T>, SourceImpl<T>() {
    // Attributs
    override var data: T = default
        protected set

    private val sink = object : Sink<T> {
        override fun updateData(data: T, origin: Source<T>) {
            this@Multiplexer.data = data
            emitData(data, origin)
        }
    }

    // Méthodes
    fun add(source: Source<T>) {
        source.addSink(sink)
    }

    fun remove(source: Source<T>) {
        source.removeSink(sink)
    }
}