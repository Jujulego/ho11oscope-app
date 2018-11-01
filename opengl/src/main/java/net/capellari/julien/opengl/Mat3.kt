package net.capellari.julien.opengl

import android.util.Log
import net.capellari.julien.opengl.base.BaseMat

class Mat3() : BaseMat<Mat3,Vec3>(3, Mat3::class, Vec3::class) {
    // Companion
    companion object {
        fun zero():     Mat3 = BaseMat.zero(Mat3::class)
        fun identity(): Mat3 = BaseMat.identity(Mat3::class)
    }

    // Constructeurs
    constructor(v0: Vec3, v1: Vec3, v2: Vec3): this() {
        this[0,0] = v0.x; this[0,1] = v0.y; this[0,2] = v0.z
        this[1,0] = v1.x; this[1,1] = v1.y; this[1,2] = v1.z
        this[2,0] = v2.x; this[2,1] = v2.y; this[2,2] = v2.z
    }
    constructor(a0: Float, a1: Float, a2: Float,
                b0: Float, b1: Float, b2: Float,
                c0: Float, c1: Float, c2: Float): this() {
        this[0,0] = a0; this[0,1] = a1; this[0,2] = a2
        this[1,0] = b0; this[1,1] = b1; this[1,2] = b2
        this[2,0] = c0; this[2,1] = c1; this[2,2] = c2
    }

    // Méthodes
    fun print(tag: String) {
        Log.d(tag, "[%.3f, %.3f, %.3f]".format(this[0,0], this[0,1], this[0,2]))
        Log.d(tag, "[%.3f, %.3f, %.3f]".format(this[1,0], this[1,1], this[1,2]))
        Log.d(tag, "[%.3f, %.3f, %.3f]".format(this[2,0], this[2,1], this[2,2]))
    }
}