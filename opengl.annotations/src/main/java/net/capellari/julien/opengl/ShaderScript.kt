package net.capellari.julien.opengl

import org.intellij.lang.annotations.Language

@Target()
@Retention(AnnotationRetention.SOURCE)
annotation class ShaderScript(val type: ShaderType, @Language("GLSL") val script: String = "", val file: String = "")