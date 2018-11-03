package net.capellari.julien.opengl

// Shader annotations
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Program(
        val mode: Int = 0,
        vararg val shaders: ShaderScript
)