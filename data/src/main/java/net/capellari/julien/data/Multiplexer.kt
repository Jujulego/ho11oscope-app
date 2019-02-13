package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

open class Multiplexer<T>(default: T): Noeud<T>, SourceImpl<T>() {
    // Attributs
    override var data: T = default
        protected set

    // Méthodes
    override fun updateData(data: T, origin: Source<T>) {
        this.data = data
        emitData(data, origin)
    }
}