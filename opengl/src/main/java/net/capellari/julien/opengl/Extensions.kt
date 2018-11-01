package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec
import java.nio.FloatBuffer

// Opérateurs inverses
operator fun <T : BaseVec<T>>   Float.times(vec: T): T = vec*this
operator fun <T : BaseMat<T,*>> Float.times(mat: T): T = mat*this

// Méthodes
fun FloatBuffer.put(v: Vec3): FloatBuffer = put(v.x).put(v.y).put(v.z)