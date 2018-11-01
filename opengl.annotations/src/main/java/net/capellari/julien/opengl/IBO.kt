package net.capellari.julien.opengl

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class IBO(val type: BufferType = BufferType.SHORT)