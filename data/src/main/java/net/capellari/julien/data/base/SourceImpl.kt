package net.capellari.julien.data.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.capellari.julien.data.Sink
import net.capellari.julien.data.Source

abstract class SourceImpl<T> : Source<T> {
    // Attributs
    private val sinks = mutableSetOf<Sink<T>>()
    private val _livedata = MutableLiveData<T>()

    // Propriétés
    override val livedata: LiveData<T> get() = _livedata

    // Méthodes
    protected fun emitData(data: T, origin: Source<T> = this) {
        _livedata.value = data
        sinks.forEach { it.updateData(data, origin) }
    }

    override fun addSink(sink: Sink<T>) {
        sinks.add(sink)
    }

    override fun removeSink(sink: Sink<T>) {
        sinks.remove(sink)
    }
}