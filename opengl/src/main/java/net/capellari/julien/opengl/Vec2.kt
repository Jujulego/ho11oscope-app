package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseVec

class Vec2() : BaseVec<Vec2>(2, Vec2::class) {
    // Propriétés
    var x: Float get() = data[0]
        set(v) { data[0] = v}

    var y: Float get() = data[1]
        set(v) { data[1] = v}

    // Constructeur
    constructor(o: Vec2) : this(o.x, o.y)
    constructor(x: Float, y: Float) : this() {
        data[0] = x; data[1] = y
    }
}