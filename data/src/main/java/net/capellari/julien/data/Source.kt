package net.capellari.julien.data

interface Source<T>: Configurable {
    // Attributs
    val data: T

    // Méthodes
    fun addSink(sink: Sink<T>, sync: Boolean = true)
    fun removeSink(sink: Sink<T>)
}