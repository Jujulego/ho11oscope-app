package net.capellari.julien.ho11oscope.opengl.objets

import android.opengl.GLES20

// Constantes
const val COORDS_PER_VERTEX = 3

// Fonctions
fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}