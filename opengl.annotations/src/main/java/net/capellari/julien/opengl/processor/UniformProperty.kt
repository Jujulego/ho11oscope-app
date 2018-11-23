package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import net.capellari.julien.opengl.Uniform
import javax.lang.model.element.VariableElement

internal class UniformProperty(element: VariableElement, val annotation: Uniform) : BaseProperty() {
    // Attributs
    lateinit var property: PropertySpec
        protected set

    val name = element.simpleName.toString()
    val type = element.asType().asTypeName()

    // Constructeur
    init {
        this.createProperty()
    }

    // Méthodes
    fun createProperty() {
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

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperty(property)
    }
}