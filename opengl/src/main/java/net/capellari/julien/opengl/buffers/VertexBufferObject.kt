package net.capellari.julien.opengl.buffers

import android.opengl.GLES31
import net.capellari.julien.opengl.base.*

class VertexBufferObject : BufferObject(GLES31.GL_ARRAY_BUFFER), ShortBO, IntBO, FloatBO {
    // Put collection
    inline fun<reified T> put(array: Collection<T>) {
        array.forEach {
            when (it) {
                is Short -> put(it as Short)
                is Int   -> put(it as Int)
                is Float -> put(it as Float)

                is BaseVec<*>   -> put(it as BaseVec<*>)
                is BaseMat<*,*> -> put(it as BaseMat<*,*>)

                else -> put(it as Any)
            }
        }
    }
}