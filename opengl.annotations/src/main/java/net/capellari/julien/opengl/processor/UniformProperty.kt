package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import net.capellari.julien.opengl.Uniform
import javax.lang.model.element.VariableElement

class UniformProperty(element: VariableElement, val annotation: Uniform) : HandleProperty(element) {
    // Méthodes
    override fun createProperty() {
        val param = ParameterSpec.builder("value", type).build()

        property = PropertySpec.builder(name, type, KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("reloadUniforms = true")
                    }.build())
                }.build()
    }
}