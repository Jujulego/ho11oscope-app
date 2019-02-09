package net.capellari.julien.data

interface Sink<T> {
    // Méthodes
    fun updateData(data: T, origin: Source<T>)
}