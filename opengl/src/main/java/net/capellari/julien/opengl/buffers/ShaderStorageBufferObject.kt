package net.capellari.julien.opengl.buffers

import android.opengl.GLES32

class ShaderStorageBufferObject : BindedBufferObject(GLES32.GL_SHADER_STORAGE_BUFFER) {
    // Attributs
    override var binding: Int
        get() = super.binding
        public set(value) { super.binding = value }
}