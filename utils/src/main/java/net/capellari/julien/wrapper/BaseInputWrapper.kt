package net.capellari.julien.wrapper

abstract class BaseInputWrapper<T> : InputWrapper<T> {
    // Attributs
    private var listeners = mutableListOf<ValueListener<T>>()

    // Méthodes
    protected fun emit(value: T) {
        listeners.forEach { it.onNewValue(value, this) }
    }

    override fun addValueListener(listener: ValueListener<T>) {
        listeners.add(listener)
    }

    override fun removeValueListener(listener: ValueListener<T>) {
        listeners.remove(listener)
    }
}