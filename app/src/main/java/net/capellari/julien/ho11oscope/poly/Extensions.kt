package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.ho11oscope.poly.opengl.ObjGeometry
import java.nio.FloatBuffer

// Extensions
fun FloatBuffer.put(v: ObjGeometry.Vec3): FloatBuffer = put(v.x).put(v.y).put(v.z)
fun FloatBuffer.put(a: Array<Float>): FloatBuffer {
    for (i in 0 until minOf(limit(), a.size)) {
        put(a[i])
    }

    return this
}