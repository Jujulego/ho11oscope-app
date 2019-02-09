package net.capellari.julien.data

import net.capellari.julien.data.base.SinkImpl
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class UnitTest {
    @Test
    fun sink() {
        // Test : G => Sk
        val source = Generator(0, 1)
        source.addSink(TestSink(1))

        source.run()
    }

    @Test
    fun transform() {
        // Test : G => T => Sk
        val trans  = Transform<Int,Int>(0) { d, _ -> d + 1 }
        trans.addSink(TestSink(2))

        val source = Generator(0, 1)
        source.addSink(trans)

        source.run()
    }

    @Test
    fun multiplexer() {
        // Test :
        // G => |
        //      |} M => Sk
        // G => |
        val sink = object : SinkImpl<Int>() {
            var sum = 0

            override fun updateData(data: Int, origin: Source<Int>) {
                sum += data
            }
        }

        val mux = Multiplexer(0)
        mux.addSink(sink)

        val gen1 = Generator(0, 1)
        gen1.addSink(mux)

        val gen2 = Generator(0, 1)
        gen2.addSink(mux)

        gen1.run()
        gen2.run()

        assertEquals(2, sink.sum)
    }

    // Classes
    class TestSink<T: Any>(val result: T): SinkImpl<T>() {
        // Méthodes
        override fun updateData(data: T, origin: Source<T>) {
            assertEquals(data, result)
        }
    }
}