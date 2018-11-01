package net.capellari.julien.opengl

import android.opengl.Matrix
import android.util.Log

class Mat4() {
    // Companion
    companion object {
        // Matrix creation
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

    // Attributs
    var data = FloatArray(16)
        private set

    // Constructeurs
    constructor(mat: FloatArray): this() {
        for (i in 0 until minOf(mat.size, 16)) data[i] = mat[i]
    }
    constructor(v0: Vec4, v1: Vec4, v2: Vec4, v4: Vec4): this() {
        data[0]  = v0.x; data[1]  = v0.y; data[2]  = v0.z; data[3]  = v0.a
        data[4]  = v1.x; data[5]  = v1.y; data[6]  = v1.z; data[7]  = v1.a
        data[8]  = v2.x; data[9]  = v2.y; data[10] = v2.z; data[11] = v2.a
        data[12] = v4.x; data[13] = v4.y; data[14] = v4.z; data[15] = v4.a
    }
    constructor(a0: Float, a1: Float, a2: Float, a3: Float,
                b0: Float, b1: Float, b2: Float, b3: Float,
                c0: Float, c1: Float, c2: Float, c3: Float,
                d0: Float, d1: Float, d2: Float, d3: Float): this() {
        data[0]  = a0; data[1]  = a1; data[2]  = a2; data[3]  = a3
        data[4]  = b0; data[5]  = b1; data[6]  = b2; data[7]  = b3
        data[8]  = c0; data[9]  = c1; data[10] = c2; data[11] = c3
        data[12] = d0; data[13] = d1; data[14] = d2; data[15] = d3
    }

    // Opérateurs
    operator fun get(col: Int, lig: Int): Float = data[lig*4 + col]
    operator fun set(col: Int, lig: Int, v: Float) { data[lig*4 + col] = v }

    // - logic
    override operator fun equals(other: Any?): Boolean = if (other is Mat4) data.contentEquals(other.data) else false

    // - math
    operator fun times(o: Mat4): Mat4 = Mat4().also { Matrix.multiplyMM(it.data, 0, data, 0, o.data, 0) }
    operator fun times(o: Vec4): Vec4 = Vec4().also { Matrix.multiplyMV(it.data, 0, data, 0, o.data, 0) }

    // Méthodes
    fun print(tag: String) {
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(data[0],  data[1],  data[2],  data[3]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(data[4],  data[5],  data[6],  data[7]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(data[8],  data[9],  data[10], data[11]))
        Log.d(tag, "[%.3f, %.3f, %.3f, %.3f]".format(data[12], data[13], data[14], data[15]))
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}