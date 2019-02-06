package net.capellari.julien.wrapper

abstract class InputWrapper<T : Any> {
    // Attributs
    private val listeners = arrayListOf<OnValueChanged<T>>()

    // Propriétés
    abstract var value: T

    // Méthodes
    protected fun emit(value: T) {
        listeners.forEach { it.onValueChanged(value) }
    }

    fun addValueListener(listener: OnValueChanged<T>) {
        listeners.add(listener)
    }

    fun removeValueListener(listener: OnValueChanged<T>) {
        listeners.remove(listener)
    }

    // Interface
    interface OnValueChanged<T> {
        fun onValueChanged(value: T)
    }
}