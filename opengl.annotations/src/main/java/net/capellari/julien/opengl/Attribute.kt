package net.capellari.julien.opengl

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Attribute(
        val name: String, val type: AttributeType = AttributeType.OTHER,
        val normalized: Boolean = false
)