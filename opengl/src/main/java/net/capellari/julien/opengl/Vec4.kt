package net.capellari.julien.opengl

import android.opengl.Matrix
import net.capellari.julien.opengl.base.BaseVec

class Vec4() : BaseVec<Vec4>(4, Vec4::class) {
    // Propriétés
    var x: Float get() = data[0]
        set(v) { data[0] = v}

    var y: Float get() = data[1]
        set(v) { data[1] = v}

    var z: Float get() = data[2]
        set(v) { data[2] = v}

    var a: Float get() = data[3]
        set(v) { data[3] = v}

    val xy get()  = Vec2(x, y)
    val xyz get() = Vec3(x, y, z)

    // Constructeur
    constructor(o: Vec4) : this() {
        data = o.data
    }
    constructor(o: Vec3, a: Float) : this(o.x, o.y, o.z, a)
    constructor(o: Vec2, z: Float, a: Float) : this(o.x, o.y, z, a)
    constructor(x: Float, y: Float, z: Float, a: Float): this() {
        data[0] = x; data[1] = y; data[2] = z; data[3] = a
    }

    // Opérateurs
    // - math
    operator fun times(o: Mat4): Vec4 = Vec4().also { Matrix.multiplyMV(it.data, 0, o.data, 0, data, 0) }
}