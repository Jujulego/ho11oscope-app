package net.capellari.julien.opengl.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import net.capellari.julien.opengl.JNITest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OpenGLAndroidTest {
    // Tests
    @Test fun test() {
        val test = JNITest()
        assertEquals(test.test(), 8)
        test.dispose()
    }
}