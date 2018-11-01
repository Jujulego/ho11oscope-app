package net.capellari.julien.opengl.base

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class BaseMat<T,V> protected constructor(val size: Int, private val subcls: KClass<T>, private val veccls: KClass<V>) where T : BaseMat<T,V>, V : BaseVec<V> {
    // Companion
    companion object {
        // Méthodes
        fun <T : BaseMat<T,*>> zero(matcls: KClass<T>): T = matcls.createInstance().apply {
            fill(0f)
        }

        fun <T : BaseMat<T,*>> identity(matcls: KClass<T>): T = matcls.createInstance().apply {
            fill { col, lig -> if (col == lig) 1f else 0f }
        }
    }

    // Attributs
    var data = FloatArray(size*size)
        protected set

    // Opérateurs
    // - acces
    operator fun get(lig: Int, col: Int): Float = data[col*size + lig]
    operator fun set(lig: Int, col: Int, v: Float) { data[col*size + lig] = v }

    // - logic
    override operator fun equals(other: Any?): Boolean = if (other is BaseMat<*,*>) data.contentEquals(other.data) else false

    // - math
    operator fun unaryMinus(): T = subcls.createInstance().also { res -> res.fill { col, lig -> -this[col,lig] } }

    operator fun plus(other: T):  T = subcls.createInstance().also { res -> res.fill { col, lig -> this[col,lig] + other[col, lig] } }
    operator fun minus(other: T): T = subcls.createInstance().also { res -> res.fill { col, lig -> this[col,lig] - other[col, lig] } }
    operator fun times(v: Float): T = subcls.createInstance().also { res -> res.fill { col, lig -> this[col,lig] * v } }
    operator fun div(v: Float):   T = subcls.createInstance().also { res -> res.fill { col, lig -> this[col,lig] / v } }

    open operator fun times(vec: V): V {
        return veccls.createInstance().also { res ->
            res.fill { col ->
                FloatArray(1).also {
                    for (i in 0 until size) it[0] += this[col,i] * vec[i]
                }[0]
            }
        }
    }

    open operator fun times(other: T): T {
        return subcls.createInstance().also { res ->
            res.fill { col, lig ->
                FloatArray(1).also {
                    for (i in 0 until size) it[0] += this[i,lig] * other[col,i]
                }[0]
            }
        }
    }

    // Méthodes
    fun fill(v: Float) = fill { _,_ -> v }
    fun fill(provider: (col: Int, lig: Int) -> Float) {
        for (col in 0 until size) {
            for (lig in 0 until size) {
                this[col,lig] = provider(col, lig)
            }
        }
    }

    fun col(col: Int): V = veccls.createInstance().also { res -> res.fill { lig -> this[col, lig] } }
    fun lig(lig: Int): V = veccls.createInstance().also { res -> res.fill { col -> this[col, lig] } }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}