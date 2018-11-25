package net.capellari.julien.opengl.processor

import net.capellari.julien.opengl.UniformBlock
import javax.lang.model.element.VariableElement

internal class UniformBlockProperty(element: VariableElement, val annotation: UniformBlock) {
    // Attributs
    val name = element.simpleName.toString()
}