package net.capellari.julien.opengl

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Attribute(
        val type: AttributeType, val name: String,
        val normalized: Boolean = false
)