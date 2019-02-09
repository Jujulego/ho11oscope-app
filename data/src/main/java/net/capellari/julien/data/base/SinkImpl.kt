package net.capellari.julien.data.base

import net.capellari.julien.data.Sink

abstract class SinkImpl<T>: Sink<T> {
    // Attributs
    override val attributs = mutableMapOf<String,Any?>()
}