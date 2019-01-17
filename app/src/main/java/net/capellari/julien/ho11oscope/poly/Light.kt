package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.Vec3
import kotlin.math.cos
import kotlin.math.sin

data class Light(var distance: Float, var angle: Float, var puissance: Int) {
    // Méthodes
    fun position(y: Float) = Vec3(distance * sin(angle), y, distance * cos(angle))
}