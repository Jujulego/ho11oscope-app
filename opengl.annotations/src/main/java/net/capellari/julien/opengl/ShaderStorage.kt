package net.capellari.julien.opengl

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ShaderStorage(val name: String = "")