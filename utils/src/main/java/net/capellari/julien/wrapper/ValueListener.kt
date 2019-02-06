package net.capellari.julien.wrapper

interface ValueListener<T> {
    // Events
    fun onNewValue(value: T, from: InputWrapper<T>)
}