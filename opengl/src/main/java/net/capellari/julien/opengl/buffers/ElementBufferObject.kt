package net.capellari.julien.opengl.buffers

import android.opengl.GLES31

class ElementBufferObject : BufferObject(GLES31.GL_ELEMENT_ARRAY_BUFFER), IntBO, ShortBO {
    // Put collection
    inline fun<reified T> put(array: Collection<T>) {
        array.forEach {
            when (it) {
                is Short -> put(it as Short)
                is Int   -> put(it as Int)

                else -> put(it as Any)
            }
        }
    }
}