package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

class Generator<T>(default: T, val generator: () -> T): Source<T>, Runnable, SourceImpl<T>() {
    // Attributs
    override var data: T = default

    // Méthodes
    override fun run() {
        data = generator()
        emitData(data)
    }
}