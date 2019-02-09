package net.capellari.julien.data

import androidx.lifecycle.LiveData

interface Source<T> {
    // Attributs
    val data: T
    val livedata: LiveData<T>

    // Méthodes
    fun addSink(sink: Sink<T>)
    fun removeSink(sink: Sink<T>)

}