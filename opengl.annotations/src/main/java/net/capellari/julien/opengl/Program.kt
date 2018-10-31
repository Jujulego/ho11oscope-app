package net.capellari.julien.opengl

// Shader annotations
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Program(vararg val shaders: ShaderScript)