package net.capellari.julien.ho11oscope.opengl

import net.capellari.julien.opengl.Color
import net.capellari.julien.opengl.Vec3
import net.capellari.julien.opengl.base.Mesh

class Triangle : Mesh(false, false) {
    // Méthodes
    override fun getVertices(): Any = arrayOf(
            Vec3(  0f,  0.622008459f, 0f),
            Vec3(-.5f, -0.311004243f, 0f),
            Vec3( .5f, -0.311004243f, 0f)
    )

    override fun getOther(name: String): Any = when(name) {
        "vColor" -> arrayOf(
                Color.RED,
                Color.GREEN,
                Color.BLUE
        )
        else -> super.getOther(name)
    }
}

class Carre : Mesh(true,  false) {
    // Méthodes
    override fun getIndices():  Any = arrayOf<Short>(0, 1, 2, 0, 2, 3)
    override fun getVertices(): Any = arrayOf(
            Vec3(-.5f,  .5f, 0f),
            Vec3(-.5f, -.5f, 0f),
            Vec3( .5f, -.5f, 0f),
            Vec3( .5f,  .5f, 0f)
    )

    override fun getOther(name: String): Any = when(name) {
        "vColor" -> arrayOf(
                Color.BLUE,
                Color.MAGENTA,
                Color.RED,
                Color.MAGENTA
        )
        else -> super.getOther(name)
    }
}

class Hexagone : Mesh(true,  false) {
    // Méthodes
    override fun getIndices(): Any = arrayOf<Short>(
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5,
            0, 5, 6,
            0, 6, 1
    )

    override fun getVertices(): Any = arrayOf(
            Vec3(   0f,    0f, 0f), // 0
            Vec3(  .5f,    0f, 0f), // 1
            Vec3( .25f,  .43f, 0f), // 2
            Vec3(-.25f,  .43f, 0f), // 3
            Vec3( -.5f,    0f, 0f), // 4
            Vec3(-.25f, -.43f, 0f), // 5
            Vec3( .25f, -.43f, 0f)  // 6
    )

    override fun getOther(name: String): Any = when(name) {
        "vColor" -> arrayOf(
                Color.WHITE,
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.YELLOW,
                Color.RED,
                Color.MAGENTA
        )
        else -> super.getOther(name)
    }
}