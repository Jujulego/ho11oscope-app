package net.capellari.julien.opengl.buffers

import android.opengl.GLES32
import net.capellari.julien.opengl.base.BaseBufferObject

abstract class BindedBufferObject(target: Int): BaseBufferObject(target) {
    // Attributs
    var binding: Int = GLES32.GL_INVALID_INDEX
        protected set

    // Méthodes
    override fun toGPU(usage: Int) {
        bind {
            super.toGPU(usage)
            GLES32.glBindBufferBase(target, binding, id)
        }
    }
}