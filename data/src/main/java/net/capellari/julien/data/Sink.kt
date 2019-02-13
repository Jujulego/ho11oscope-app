package net.capellari.julien.data

interface Sink<T>: Configurable {
    // Méthodes
    fun updateData(data: T, origin: Source<T>)
}