package net.capellari.julien.wrapper

interface NumberWrapper<T: Number> : InputWrapper<T> {
    // Attributs
    var min: T
    var max: T
}