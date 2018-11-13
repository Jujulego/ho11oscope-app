package net.capellari.julien.opengl

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Program(
        val mode: Int = 0,
        vararg val shaders: ShaderScript
)