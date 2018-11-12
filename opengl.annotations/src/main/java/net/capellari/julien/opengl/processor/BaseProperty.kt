package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.TypeSpec

abstract class BaseProperty {
    // Méthodes
    abstract fun addProperties(type: TypeSpec.Builder)
}