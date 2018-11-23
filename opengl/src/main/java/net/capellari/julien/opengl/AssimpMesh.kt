package net.capellari.julien.opengl

import net.capellari.julien.opengl.Material
import net.capellari.julien.opengl.base.BaseMesh
import net.capellari.julien.opengl.jni.JNIMesh

class AssimpMesh(val mesh: JNIMesh) : BaseMesh(true, true) {
    override fun getMaterial(): Material {
        return Material("")
    }

    override fun getVertices(): Any {
        return mesh.vertices
    }

    override fun getNormals(): Any {
        return mesh.normals
    }

    override fun getIndices(): Any {
        return mesh.indices
    }
}