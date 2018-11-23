package net.capellari.julien.opengl

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Program(
        val mode: Int = 0,
        val shaders: Shaders,
        vararg val attributs: Attribute
)