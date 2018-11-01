package net.capellari.julien.opengl

import android.util.Log
import net.capellari.julien.opengl.base.BaseMat

class Mat2() : BaseMat<Mat2,Vec2>(2, Mat2::class, Vec2::class) {
    // Companion
    companion object {
        fun zero():     Mat2 = BaseMat.zero(Mat2::class)
        fun identity(): Mat2 = BaseMat.identity(Mat2::class)
    }

    // Constructeurs
    constructor(v0: Vec2, v1: Vec2): this() {
        this[0,0] = v0.x; this[0,1] = v0.y
        this[1,0] = v1.x; this[1,1] = v1.y
    }
    constructor(a0: Float, a1: Float,
                b0: Float, b1: Float): this() {
        this[0,0] = a0; this[0,1] = a1
        this[1,0] = b0; this[1,1] = b1
    }

    // Méthodes
    fun print(tag: String) {
        Log.d(tag, "[%.3f, %.3f]".format(this[0,0], this[0,1]))
        Log.d(tag, "[%.3f, %.3f]".format(this[1,0], this[1,1]))
    }
}