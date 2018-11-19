package net.capellari.julien.opengl

import net.capellari.julien.opengl.buffers.BufferObject

interface BaseStructure {
    // Méthodes
    fun toBuffer(buffer: BufferObject)
    fun getBufferSize(): Int
}