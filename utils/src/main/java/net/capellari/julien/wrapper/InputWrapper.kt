package net.capellari.julien.wrapper

interface InputWrapper<T>: ValueHolder<T> {
    // Méthodes
    fun addValueListener(listener: ValueListener<T>)
    fun removeValueListener(listener: ValueListener<T>)
}