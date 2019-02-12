package net.capellari.julien.data.base

import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source

abstract class SourceImpl<T>: Source<T>, ConfigurableImpl() {
    // Attributs
    private val sinks = mutableSetOf<Sink<T>>()

    // Méthodes
    protected fun emitData(data: T) = emitData(data, this)
    protected fun emitData(data: T, origin: Source<T>) {
        sinks.forEach { it.updateData(data, origin) }
    }

    override fun addSink(sink: Sink<T>) {
        sinks.add(sink)
        sink.updateData(data, this)
    }

    override fun removeSink(sink: Sink<T>) {
        sinks.remove(sink)
    }
}