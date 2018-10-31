package net.capellari.julien.opengl.mtl

class MtlParseException : Exception {
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Exception) : super(msg, cause)
}