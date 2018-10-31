package net.capellari.julien.opengl

import java.nio.FloatBuffer

// Opérateurs inverses
operator fun Float.times(v: Vec2) = v*this
operator fun Float.times(v: Vec3) = v*this

// Méthodes
fun FloatBuffer.put(v: Vec3): FloatBuffer = put(v.x).put(v.y).put(v.z)