package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import net.capellari.julien.opengl.SharedStorage
import javax.lang.model.element.VariableElement

@RequiresApi(26)
internal class ShaderStorageProperty(element: VariableElement, val annotation: SharedStorage) {
    // Attributs
    val name = element.simpleName.toString()
}