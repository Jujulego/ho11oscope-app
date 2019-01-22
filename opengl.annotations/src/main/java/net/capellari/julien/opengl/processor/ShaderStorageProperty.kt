package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import net.capellari.julien.opengl.ShaderStorage
import javax.lang.model.element.VariableElement

@RequiresApi(26)
internal class ShaderStorageProperty(element: VariableElement, val annotation: ShaderStorage) {
    // Attributs
    val name = element.simpleName.toString()
}