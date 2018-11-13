package net.capellari.julien.opengl

import net.capellari.julien.opengl.buffers.BufferObject

abstract class BaseStructure {
    // Méthodes
    abstract fun toBuffer(buffer: BufferObject)
    abstract fun getBufferSize(): Int
}