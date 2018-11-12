package net.capellari.julien.opengl.processor

import com.squareup.kotlinpoet.TypeSpec

fun TypeSpec.Builder.addProperties(prop: BaseProperty)
        = prop.addProperties(this)

fun TypeSpec.Builder.addProperties(props: Collection<BaseProperty>)
        = props.forEach { it.addProperties(this) }