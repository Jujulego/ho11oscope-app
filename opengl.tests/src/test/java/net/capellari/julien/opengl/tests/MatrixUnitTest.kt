package net.capellari.julien.opengl.tests

import net.capellari.julien.opengl.*
import org.junit.Assert.*
import org.junit.Test

class MatrixUnitTest {
    // Attributs
    val mat = Mat3(1f, 2f, 3f,
                   4f, 5f, 6f,
                   7f, 8f, 9f)

    // Tests
    // - acces
    @Test fun get() {
        assertEquals(1f, mat[0, 0])
        assertEquals(2f, mat[0, 1])
        assertEquals(3f, mat[0, 2])
        assertEquals(4f, mat[1, 0])
        assertEquals(5f, mat[1, 1])
        assertEquals(6f, mat[1, 2])
        assertEquals(7f, mat[2, 0])
        assertEquals(8f, mat[2, 1])
        assertEquals(9f, mat[2, 2])
    }

    @Test fun col() {
        assertEquals(Vec3(1f, 2f, 3f), mat.col(0))
        assertEquals(Vec3(4f, 5f, 6f), mat.col(1))
        assertEquals(Vec3(7f, 8f, 9f), mat.col(2))
    }

    @Test fun lig() {
        assertEquals(Vec3(1f, 4f, 7f), mat.lig(0))
        assertEquals(Vec3(2f, 5f, 8f), mat.lig(1))
        assertEquals(Vec3(3f, 6f, 9f), mat.lig(2))
    }

    // - opérations
    @Test fun plus() {
        val res = Mat3(2f,  4f,  6f,
                       8f, 10f, 12f,
                       14f, 16f, 18f)

        assertEquals(res, mat + mat)
    }

    @Test fun minus() {
        assertEquals(Mat3(), mat - mat)
    }

    @Test fun times() {
        // - float
        var res = Mat3(2f,  4f,  6f,
                       8f, 10f, 12f,
                       14f, 16f, 18f)

        assertEquals(res, mat * 2f)
        assertEquals(res, 2f * mat)

        // - vecteur
        val vec = Vec3(1f, 2f, 3f)

        assertEquals(Vec3(30f, 36f, 42f), vec * mat)
        assertEquals(Vec3(14f, 32f, 50f), mat * vec)

        // - matrice
        res = Mat3(30f,  36f,  42f,
                   66f,  81f,  96f,
                   102f, 126f, 150f)

        assertEquals(res, mat * mat)
    }

    // - generation
    @Test fun identity() {
        val res = Mat3(1f, 0f, 0f,
                       0f, 1f, 0f,
                       0f, 0f, 1f)

        assertEquals(res, Mat3.identity())
    }
}