package net.capellari.julien.opengl.buffers

import android.opengl.GLES31
import net.capellari.julien.opengl.base.BaseBufferObject

abstract class BindedBufferObject(target: Int): BaseBufferObject(target) {
    // Attributs
    var binding: Int = GLES31.GL_INVALID_INDEX
        protected set

    // Méthodes
    override fun toGPU(usage: Int) {
        bind {
            super.toGPU(usage)
            GLES31.glBindBufferBase(target, binding, id)
        }
    }
}