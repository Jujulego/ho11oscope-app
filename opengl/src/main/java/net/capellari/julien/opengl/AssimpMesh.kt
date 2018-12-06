package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseMesh
import net.capellari.julien.opengl.jni.JNIMesh

class AssimpMesh(val mesh: JNIMesh) : BaseMesh(true, true, true) {
    // Attributs
    private lateinit var material: Material

    // Méthodes
    override fun getMaterial(): Material {
        if (!::material.isInitialized) {
            material = mesh.material
        }

        return material
    }

    override fun getVertices(): Any {
        return mesh.vertices
    }

    override fun getNormals(): Any {
        return mesh.normals
    }

    override fun getTexCoords() : Any {
        return mesh.texCoords
    }

    override fun getIndices(): Any {
        return mesh.indices
    }
}