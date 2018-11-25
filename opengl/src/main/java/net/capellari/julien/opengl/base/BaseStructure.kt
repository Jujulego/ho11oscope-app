package net.capellari.julien.opengl.base

interface BaseStructure {
    // Méthodes
    fun toBuffer(buffer: BaseBufferObject)
    fun getBufferSize(): Int
    fun toUniform(nom: String, program: BaseProgram)
}