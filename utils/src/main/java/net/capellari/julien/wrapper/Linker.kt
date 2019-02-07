package net.capellari.julien.wrapper

import androidx.annotation.CallSuper

open class Linker<T, W : InputWrapper<T>>(default: T) : BaseInputWrapper<T>() {
    // Attributs
    protected val wrappers = mutableListOf<W>()
    protected val printers = mutableListOf<Printer<T>>()

    private val listener = object : ValueListener<T> {
        @Suppress("UNCHECKED_CAST")
        override fun onNewValue(value: T, from: InputWrapper<T>) {
            // Save value
            this@Linker.value = value
            emit(value)

            // And propagate it
            propagate(value, from as W)
        }
    }

    // Events
    @CallSuper
    protected open fun onAddWrapper(wrapper: W) {
        wrapper.addValueListener(listener)

        wrappers.add(wrapper)
        wrapper.value = value
    }

    @CallSuper
    protected open fun onRemoveWrapper(wrapper: W) {
        wrappers.remove(wrapper)

        wrapper.removeValueListener(listener)
    }

    @CallSuper
    protected open fun onAddPrinter(printer: Printer<T>) {
        printers.add(printer)
        printer.value = value
    }

    @CallSuper
    protected open fun onRemovePrinter(printer: Printer<T>) {
        printers.remove(printer)
    }

    // Méthodes
    fun add(wrapper: W, keep: Boolean = false) {
        if (keep) keep(wrapper)
        onAddWrapper(wrapper)
    }
    fun remove(wrapper: W) { onRemoveWrapper(wrapper) }

    fun add(printer: Printer<T>) { onAddPrinter(printer) }
    fun remove(printer: Printer<T>) { onRemovePrinter(printer) }

    @CallSuper
    protected open fun keep(wrapper: W) {
        value = wrapper.value
    }

    protected fun propagate(value: T, origin: W? = null) {
        printers.forEach { it.value = value }

        wrappers.filter { it != origin }
                .forEach { it.value = value }
    }

    // Propriétés
    override var value: T = default
        set(value) {
            field = value
            propagate(value)
        }
}