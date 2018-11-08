package net.capellari.julien.utils

import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

inline val <T : Any> KProperty0<T>.sharedPreference: String?
    get() {
        isAccessible = true
        val delegate = getDelegate()
        isAccessible = false

        return if (delegate is BaseSharedPreference<*,*>) delegate.name else null
    }