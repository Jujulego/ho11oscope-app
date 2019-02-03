package net.capellari.julien.opengl.base

interface Structure {
    // Méthodes
    fun toBuffer(buffer: BufferObject)
    fun getBufferSize(): Int
    fun toUniform(nom: String, program: Program)
}