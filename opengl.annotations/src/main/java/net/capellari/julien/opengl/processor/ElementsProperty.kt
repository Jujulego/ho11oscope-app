package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.*
import net.capellari.julien.opengl.Elements
import javax.lang.model.element.VariableElement

internal class ElementsProperty(element: VariableElement, val annotation: Elements) : BaseProperty() {
    // Attributs
    lateinit var property: PropertySpec
        private set

    val name = element.simpleName.toString()
    val type = element.asType().asTypeName().asNullable()

    // Constructeur
    init {
        createProperty()
    }

    // Méthodes
    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperty(property)
    }

    private fun createProperty() {
        val param = ParameterSpec.builder("value", type).build()

        property = PropertySpec.builder(name, type, KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("reloadIBO = true")
                    }.build())
                }.build()
    }
}