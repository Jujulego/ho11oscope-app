package net.capellari.julien.ho11oscope.opengl.objets

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// Valeurs
var squareCoords = floatArrayOf(
       -0.5f,  0.5f, 0.0f,      // top left
       -0.5f, -0.5f, 0.0f,      // bottom left
        0.5f, -0.5f, 0.0f,      // bottom right
        0.5f,  0.5f, 0.0f       // top right
)

class Square {
    // Attributs
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    private val vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(squareCoords.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(squareCoords)
                    position(0)
                }
            }

    private val drawListBuffer: ShortBuffer =
            ByteBuffer.allocateDirect(drawOrder.size * 2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(drawOrder)
                    position(0)
                }
            }
}