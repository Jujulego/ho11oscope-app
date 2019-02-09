package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

class Transform<F,T>(default: T, val transform: (data: F, origin: Source<F>) -> T): Sink<F>, Source<T>, SourceImpl<T>() {
    // Attributs
    override var data: T = default
        private set

    // Méthodes
    override fun updateData(data: F, origin: Source<F>) {
        this.data = transform(data, origin)
        emitData(this.data)
    }
}