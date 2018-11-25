package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement

@RequiresApi(26)
internal abstract class BufferProperty(name: String) : BaseProperty() {
    // Attributs
    abstract val log: String
    val properties = ArrayList<PropertySpec>()

    lateinit var bo: PropertySpec
        protected set

    // Construteur
    init {
        this.createBO(name)
    }

    // Méthodes
    protected abstract fun createBO(name: String)
    abstract fun bindFunc(func: FunSpec.Builder)

    fun add(element: VariableElement) {
        val name = element.simpleName.toString()
        val type = Utils.getTypeName(element.asType().asTypeName())
        val param = ParameterSpec.builder("value", type).build()

        properties.add(PropertySpec.builder(name, type, KModifier.OVERRIDE)
                .apply {
                    mutable()
                    initializer("super.$name")

                    setter(FunSpec.builder("set()").apply {
                        addParameter(param)

                        addStatement("field = %N", param)
                        addStatement("%N.reload = true", bo)
                    }.build())
                }.build())
    }

    fun genBufferFunc(func: FunSpec.Builder) {
        func.addStatement("%N.generate()", bo)
    }

    fun loadBufferFunc(func: FunSpec.Builder) {
        func.beginControlFlow("if (%N.reload)", bo)
            // Compute ssbo size
            func.addStatement("var size = 0")
            for (p in properties) {
                func.addStatement("size += GLUtils.bufferSize(%N)", p)
            }

            // Allocate BO
            func.addStatement("%N.allocate(size)", bo)
            func.addStatement("%N.position = 0", bo)

            // Put data
            for (p in properties) {
                func.addStatement("%N.put(%N)", bo, p)
            }

            // Bind SSBO
            func.addStatement("%N.toGPU()", bo)
            func.addStatement("GLUtils.checkGlError(%S)", "Loading $log")

            func.addStatement("%N.reload = false", bo)
        func.endControlFlow()
    }

    override fun addProperties(type: TypeSpec.Builder) {
        type.addProperties(properties)
        type.addProperty(bo)
    }
}