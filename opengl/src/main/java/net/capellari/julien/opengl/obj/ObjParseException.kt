package net.capellari.julien.opengl.obj

class ObjParseException : Exception {
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Exception) : super(msg, cause)
}