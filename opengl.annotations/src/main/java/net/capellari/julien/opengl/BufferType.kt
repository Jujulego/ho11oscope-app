package net.capellari.julien.opengl

import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import kotlin.reflect.KClass

enum class BufferType(val size: Int, val cls: KClass<*>) {
    FLOAT(4, FloatBuffer::class),
    INT(  4, IntBuffer::class),
    SHORT(2, ShortBuffer::class)
}