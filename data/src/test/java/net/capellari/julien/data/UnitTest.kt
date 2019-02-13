package net.capellari.julien.data

import net.capellari.julien.data.utils.StringToIntTransform
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
        val sink = TestSink(1)

        val source = Generator(0, 1)
        source.addSink(sink, sync = false)

        source.run()

        // Propriété
        sink["test"] = "test"
        assertEquals("test", sink.test)
    }

    @Test
    fun transform() {
        // Test : G => T => Sk
        val trans  = Transform<Int,Int>(0) { d, _ -> d + 1 }
        trans.addSink(TestSink(2), sync = false)

        val source = Generator(0, 1)
        source.addSink(trans, sync = false)

        source.run()
    }

    @Test
    fun multiplexer() {
        // Test :
        // G => |
        //      |} M => Sk
        // G => |
        val sink = object : Sink<Int> {
            var sum = 0

            override fun updateData(data: Int, origin: Source<Int>) {
                sum += data
            }
        }

        val mux = Multiplexer(0)
        mux.addSink(sink, sync = false)

        val gen1 = Generator(0, 1)
        gen1.addSink(mux)

        val gen2 = Generator(0, 1)
        gen2.addSink(mux)

        gen1.run()
        gen2.run()

        assertEquals(2, sink.sum)
    }

    @Test
    fun linker() {
        // Test :
        // G  => |
        // Sk <= |} L
        // G  => |
        val gen1 = ValueGenerator(1)
        val gen2 = ValueGenerator(1)
        gen2["test"] = "carotte"

        val sink = TestSink(1)

        val linker = Linker(0)
        linker.link(gen1)
        linker.link(gen2, keep = true)
        linker.link(sink, sync = false)

        // Propriétés
        assertEquals("carotte", linker["test"])

        linker.setProp("test", "banane")
        assertEquals("banane", linker["test"])
        assertEquals("banane", gen1.test)

        // Valeur
        linker.data = 1
        gen1.run()
    }

    @Test
    fun stringToIntTransform() {
        // G => S2I => Sk
        val sink = TestSink(1)

        val trans = StringToIntTransform(0)
        trans.addSink(sink, sync = false)

        val gen1 = Generator("", "1")
        gen1.addSink(trans, sync = false)

        gen1.run()

        val gen2 = Generator("", "50")
        gen2.addSink(trans, sync = false)
        trans["max"] = 1

        gen2.run()
    }

    // Classes
    class TestSink<T: Any>(val result: T): Sink<T> {
        // Propriétés
        @Property var test: String = ""

        // Méthodes
        override fun updateData(data: T, origin: Source<T>) {
            assertEquals(result, data)
        }
    }

    class ValueGenerator<T>(val value: T): Generator<T>(value, value) {
        // Propriétés
        @Property var test: String = ""
    }
}