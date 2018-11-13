package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseVec

class Vec3() : BaseVec<Vec3>(3, Vec3::class) {
    // Propriétés
    var x: Float get() = data[0]
        set(v) { data[0] = v}

    var y: Float get() = data[1]
        set(v) { data[1] = v}

    var z: Float get() = data[2]
        set(v) { data[2] = v}

    val xy get() = Vec2(x, y)

    // Constructeur
    constructor(o: Vec3) : this(o.x, o.y, o.z)
    constructor(o: Vec2, z: Float) : this(o.x, o.y, z)
    constructor(x: Float, y: Float, z: Float): this() {
        data[0] = x; data[1] = y; data[2] = z
    }

    // Infixes
    infix fun cross(other: Vec3): Vec3 = Vec3(
            (y * other.z) - (z * other.y),
            (z * other.x) - (x * other.z),
            (x * other.y) - (y * other.x)
    )
}