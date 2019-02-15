package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

open class Generator<T>(default: T, val generator: () -> T): Runnable, SourceImpl<T>() {
    // Attributs
    override var data: T = default

    // Constructeur
    constructor(default: T, value: T): this(default, { value })

    // Méthodes
    override fun run() {
        data = generator()
        emitData(data)
    }
}