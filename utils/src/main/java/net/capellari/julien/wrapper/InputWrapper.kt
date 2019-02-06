package net.capellari.julien.wrapper

interface InputWrapper<T> {
    // Attributs
    var value: T

    // Méthodes
    fun addValueListener(listener: ValueListener<T>)
    fun removeValueListener(listener: ValueListener<T>)
}