package net.capellari.julien.opengl.processor

import net.capellari.julien.opengl.SharedStorage
import javax.lang.model.element.VariableElement

internal class ShaderStorageProperty(element: VariableElement, val annotation: SharedStorage) {
    // Attributs
    val name = element.simpleName.toString()
}