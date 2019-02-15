package net.capellari.julien.data

import net.capellari.julien.data.base.SourceImpl

open class Transform<F,T>(default: T, val transform: (data: F, origin: Source<F>) -> T): Sink<F>, SourceImpl<T>() {
    // Attributs
    override var data: T = default
        protected set

    // Constructeur
    constructor(default: T) : this(default, { _, _ -> default })

    // Méthodes
    protected open fun applyTransform(data: F, origin: Source<F>): T {
        return transform(data, origin)
    }

    override fun updateData(data: F, origin: Source<F>) {
        this.data = applyTransform(data, origin)
        emitData(this.data)
    }
}