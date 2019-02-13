package net.capellari.julien.data

import net.capellari.julien.data.base.NoeudImpl

open class Converter<F,T>(
        protected val f2t: Transform<F,T>,
        protected val t2f: Transform<T,F>, noeud: Noeud<F>) : NoeudImpl<T>() {

    // Attributs
    private val internal_sink = object : Sink<T> {
        override fun updateData(data: T, origin: Source<T>) {
            emitData(data, origin)
        }
    }

    // Propriétés
    override val data: T get() = f2t.data

    // Initialisation
    init {
        // F => T
        noeud.addSink(f2t)
        f2t.addSink(internal_sink)

        // T => F
        t2f.addSink(noeud)
    }

    // Méthodes
    override fun updateData(data: T, origin: Source<T>) {
        t2f.updateData(data, origin)
    }
}