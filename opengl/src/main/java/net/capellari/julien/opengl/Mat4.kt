package net.capellari.julien.opengl

import android.opengl.Matrix
import android.util.Log
import net.capellari.julien.opengl.base.BaseMat

class Mat4() : BaseMat<Mat4,Vec4>(4, Mat4::class, Vec4::class) {
    // Companion
    companion object {
        // Matrix creation
        fun zero():     Mat4 = BaseMat.zero(Mat4::class)
        fun identity(): Mat4 = Mat4().apply { Matrix.setIdentityM(data, 0) }

        fun rotate(angle: Float, axis: Vec3): Mat4 = Mat4().apply {
            Matrix.setRotateM(data, 0, angle, axis.x, axis.y, axis.z)
        }
        fun rotate(angle: Float, x: Float, y: Float, z: Float): Mat4 = Mat4().apply {
            Matrix.setRotateM(data, 0, angle, x, y, z)
        }

        fun lookAt(eye: Vec3, target: Vec3, up: Vec3): Mat4 = Mat4().apply {
                Matrix.setLookAtM(data, 0,
                        eye.x,    eye.y,    eye.z,
                        target.x, target.y, target.z,
                        up.x,     up.y,     up.z
                )
            }
        fun lookAt(eyeX:    Float, eyeY:    Float, eyeZ:    Float,
                   targetX: Float, targetY: Float, targetZ: Float,
                   upX:     Float, upY:     Float, upZ:     Float): Mat4 = Mat4().apply {
                Matrix.setLookAtM(data, 0,
                        eyeX,    eyeY,    eyeZ,
                        targetX, targetY, targetZ,
                        upX,     upY,     upZ
                )
            }

        fun perspective(fovY: Float, ratio: Float, nearClip: Float, farClip: Float): Mat4 = Mat4().apply {
                Matrix.perspectiveM(data, 0, fovY, ratio, nearClip, farClip)
            }
    }

    // Constructeurs
    constructor(v0: Vec4, v1: Vec4, v2: Vec4, v3: Vec4): this() {
        this[0,0] = v0.x; this[0,1] = v0.y; this[0,2] = v0.z; this[0,3] = v0.a
        this[1,0] = v1.x; this[1,1] = v1.y; this[1,2] = v1.z; this[1,3] = v1.a
        this[2,0] = v2.x; this[2,1] = v2.y; this[2,2] = v2.z; this[2,3] = v2.a
        this[3,0] = v3.x; this[3,1] = v3.y; this[3,2] = v3.z; this[3,3] = v3.a
    }
    constructor(a0: Float, a1: Float, a2: Float, a3: Float,
                b0: Float, b1: Float, b2: Float, b3: Float,
                c0: Float, c1: Float, c2: Float, c3: Float,
                d0: Float, d1: Float, d2: Float, d3: Float): this() {
        this[0,0] = a0; this[0,1] = a1; this[0,2] = a2; this[0,3] = a3
        this[1,0] = b0; this[1,1] = b1; this[1,2] = b2; this[1,3] = b3
        this[2,0] = c0; this[2,1] = c1; this[2,2] = c2; this[2,3] = c3
        this[3,0] = d0; this[3,1] = d1; this[3,2] = d2; this[3,3] = d3
    }

    // Opérateurs
    // - math
    override operator fun times(vec: Vec4): Vec4 = Vec4().also {
        Matrix.multiplyMV(it.data, 0, data, 0, vec.data, 0)
    }
    override operator fun times(other: Mat4): Mat4 = Mat4().also {
        Matrix.multiplyMM(it.data, 0, data, 0, other.data, 0)
    }

    // Méthodes
    fun print(tag: String) {
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(this[0,0], this[0,1], this[0,2], this[0,3]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(this[1,0], this[1,1], this[1,2], this[1,3]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(this[2,0], this[2,1], this[2,2], this[2,3]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(this[3,0], this[3,1], this[3,2], this[3,3]))
    }
}