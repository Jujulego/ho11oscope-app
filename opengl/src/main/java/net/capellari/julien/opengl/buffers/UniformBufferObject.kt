package net.capellari.julien.opengl.buffers

import android.opengl.GLES31
import net.capellari.julien.opengl.base.*

class UniformBufferObject : BufferObject(GLES31.GL_UNIFORM_BUFFER), IntBO, FloatBO {
    // Put collection
    inline fun<reified T> put(array: Collection<T>) {
        array.forEach {
            when (it) {
                is Int   -> put(it as Int)
                is Float -> put(it as Float)

                is BaseVec<*> -> put(it as BaseVec<*>)
                is BaseMat<*,*> -> put(it as BaseMat<*,*>)

                else -> put(it as Any)
            }
        }
    }
}