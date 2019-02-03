package net.capellari.julien.opengl.buffers

import android.opengl.GLES32
import net.capellari.julien.opengl.base.BufferObject

abstract class BindedBufferObject(target: Int): BufferObject(target) {
    // Attributs
    open var binding: Int = GLES32.GL_INVALID_INDEX
        protected set

    // Méthodes
    override fun toGPU(usage: Int) {
        bind {
            super.toGPU(usage)
            GLES32.glBindBufferBase(target, binding, id)
        }
    }
}