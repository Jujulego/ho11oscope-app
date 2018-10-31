package net.capellari.julien.opengl.base

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class BaseVec<T> protected constructor(val size: Int, private val subcls: KClass<T>) where T : BaseVec<T> {
    // Attributs
    var data: FloatArray = FloatArray(size)
        protected set

    // Opérateurs
    // - acces
    operator fun get(i: Int): Float = data[i]

    // - logic
    override operator fun equals(other: Any?): Boolean = if (other is BaseVec<*>) data.contentEquals(other.data) else false

    // - math
    operator fun unaryMinus(): T = subcls.createInstance().also { res -> res.data = FloatArray(size) { -data[it] } }

    operator fun plus(other: T):  T = subcls.createInstance().also { res -> res.data = FloatArray(size) { data[it] + other.data[it] } }
    operator fun minus(other: T): T = subcls.createInstance().also { res -> res.data = FloatArray(size) { data[it] - other.data[it] } }
    operator fun times(v: Float): T = subcls.createInstance().also { res -> res.data = FloatArray(size) { data[it] * v } }
    operator fun div(v: Float):   T = subcls.createInstance().also { res -> res.data = FloatArray(size) { data[it] / v } }

    // Infixes
    infix fun center(other: T): T = (this + other) / 2f
    infix fun dot(other: T): Float {
        var r = 0f

        for (i in 0 until size) {
            r += data[i] * other.data[i]
        }

        return r
    }

    // Méthodes
    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    override fun toString(): String = StringBuilder("(")
            .apply {
                for (i in 0 until size) {
                    append("%.3f".format(data[i]), if (i != size-1) "," else ")")
                }
            }.toString()
}