package net.capellari.julien.opengl

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Shaders(
        vararg val scripts: ShaderScript
)