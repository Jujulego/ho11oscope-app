package net.capellari.julien.opengl.buffers

import android.opengl.GLES31

class UniformBufferObject : BindedBufferObject(GLES31.GL_UNIFORM_BUFFER) {
    // Companion
    companion object {
        private val bindings = mutableSetOf<Int>()

        fun addBinding(binding: Int) {
            synchronized(this) {
                bindings.add(binding)
            }
        }
        private fun getBinding(): Int {
            var binding = 0

            synchronized(this) {
                while (bindings.contains(binding)) ++binding
                bindings.add(binding)
            }

            return binding
        }
    }

    // Constructeur
    init {
        binding = getBinding()
    }
}