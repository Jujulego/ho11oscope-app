package net.capellari.julien.opengl

@Target()
@Retention(AnnotationRetention.SOURCE)
annotation class ShaderScript(val type: ShaderType, val script: String = "", val file: String = "")