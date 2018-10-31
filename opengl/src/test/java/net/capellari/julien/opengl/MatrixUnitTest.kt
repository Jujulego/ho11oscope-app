package net.capellari.julien.opengl

import org.junit.Assert.*
import org.junit.Test

class MatrixUnitTest {
    // Attributs
    val matrix = Mat4(1f, 5f,  9f, 13f,
                      2f, 6f, 10f, 14f,
                      3f, 7f, 11f, 15f,
                      4f, 8f, 12f, 16f)

    // Tests
    @Test fun get() {
        assertEquals( 1f, matrix[0, 0])
        assertEquals( 2f, matrix[0, 1])
        assertEquals( 3f, matrix[0, 2])
        assertEquals( 4f, matrix[0, 3])
        assertEquals( 5f, matrix[1, 0])
        assertEquals( 6f, matrix[1, 1])
        assertEquals( 7f, matrix[1, 2])
        assertEquals( 8f, matrix[1, 3])
        assertEquals( 9f, matrix[2, 0])
        assertEquals(10f, matrix[2, 1])
        assertEquals(11f, matrix[2, 2])
        assertEquals(12f, matrix[2, 3])
        assertEquals(13f, matrix[3, 0])
        assertEquals(14f, matrix[3, 1])
        assertEquals(15f, matrix[3, 2])
        assertEquals(16f, matrix[3, 3])
    }
}