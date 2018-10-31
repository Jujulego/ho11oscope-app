package net.capellari.julien.opengl.tests

import androidx.test.runner.AndroidJUnit4
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.Vec4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixUnitAndroidTest {
    // Attributs
    val matrix = Mat4(
            1f, 5f, 9f, 13f,
            2f, 6f, 10f, 14f,
            3f, 7f, 11f, 15f,
            4f, 8f, 12f, 16f
    )

    // Tests
    @Test fun identity() {
        val id = Mat4(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        )

        assertEquals(id, Mat4.identity())
    }

    @Test fun times() {
        // Matrices
        assertEquals(matrix, matrix * Mat4.identity())

        val res = Mat4(
                 90f, 202f, 314f, 426f,
                100f, 228f, 356f, 484f,
                110f, 254f, 398f, 542f,
                120f, 280f, 440f, 600f
        )
        assertEquals(res, matrix * matrix)

        // Vecteurs
        val vec = Vec4(1f, 2f, 3f, 4f)
        assertEquals(vec, vec * Mat4.identity())
    }
}