package net.capellari.julien.opengl

import org.junit.Assert.*
import org.junit.Test

class VectorUnitTest {
    // Attributs
    val t1 = Vec3(1f, 1f, 1f)
    val t2 = Vec3(1f, 0f, -1f)

    // Tests
    // - math opérators
    @Test fun unaryMinus() {
        val res = Vec3(-1f, -1f, -1f)
        assertEquals(res, -t1)
    }

    @Test fun plus() {
        val res = Vec3(2f, 2f, 2f)
        assertEquals(res, t1 + t1)
    }
    @Test fun minus() {
        val res = Vec3()
        assertEquals(res, t1 - t1)
    }
    @Test fun times() {
        val res = Vec3(2f, 2f, 2f)
        assertEquals(res, t1 * 2f)
    }
    @Test fun div() {
        val res = Vec3(.5f, .5f, .5f)
        assertEquals(res, t1 / 2f)
    }

    // - infix
    @Test fun center() {
        val res = Vec3(1f, .5f, 0f)
        assertEquals(res, t1 center t2)
    }
    @Test fun dot() {
        assertEquals(0f, t1 dot t2)
    }
    @Test fun vect() {
        val res = Vec3(-1f, 2f, -1f)
        assertEquals(res, t1 vect t2)
    }
}