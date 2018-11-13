package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.Attribute
import javax.lang.model.element.VariableElement

internal class AttributeProperty(element: VariableElement, val annotation: Attribute) : HandleProperty(element) {
    // Méthodes
    override fun createProperty() {
        val param = ParameterSpec.builder("value", type).build()

        property = PropertySpec.builder(name, type.asNullable(), KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("reloadVBO = true")
                    }.build())
                }.build()
    }

    override fun getLocationFunc(func: FunSpec.Builder) {
        func.addStatement("%N = getAttribLocation(%S)", handle, annotation.name)
    }
}