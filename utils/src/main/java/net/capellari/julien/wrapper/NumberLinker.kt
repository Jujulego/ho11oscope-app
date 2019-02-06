package net.capellari.julien.wrapper

open class NumberLinker<T: Number>(default: T) : Linker<T, NumberWrapper<T>>(default), NumberWrapper<T> {
    // Constructeurs
    constructor(default: T, max: T, min: T = default): this(default) {
        this.max = max
        this.min = min
    }

    // Events
    override fun onAddWrapper(wrapper: NumberWrapper<T>) {
        super.onAddWrapper(wrapper)

        wrapper.max = max
        wrapper.min = min
    }

    // Méthodes
    override fun keep(wrapper: NumberWrapper<T>) {
        super.keep(wrapper)

        min = wrapper.min
        max = wrapper.max
    }

    protected fun propagateMin(min: T, origin: NumberWrapper<T>? = null) {
        wrappers.filter { it != origin }
                .forEach { it.min = min }
    }

    protected fun propagateMax(max: T, origin: NumberWrapper<T>? = null) {
        wrappers.filter { it != origin }
                .forEach { it.max = max }
    }

    // Propriétés
    final override var min: T = default
        set(value) {
            field = value
            propagateMin(min)
        }

    final override var max: T = default
        set(value) {
            field = value
            propagateMax(max)
        }
}