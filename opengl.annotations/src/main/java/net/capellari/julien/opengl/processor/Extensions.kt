package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.addProperties(prop: BaseProperty)
        = prop.addProperties(this)

internal fun TypeSpec.Builder.addProperties(props: Collection<BaseProperty>)
        = props.forEach { it.addProperties(this) }