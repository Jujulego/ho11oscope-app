//
// Created by julien on 17/11/2018.
//
#include "mesh.h"

#include <algorithm>

// Constructeurs
Mesh::Mesh() {}
Mesh::Mesh(aiMesh* mesh) {
    // Process data
    // - material
    material = mesh->mMaterialIndex;

    // - vertices
    vertices.reserve(mesh->mNumVertices);
    for (unsigned int i = 0; i < mesh->mNumVertices; ++i) {
        Vertex vertex;

        vertex.position = mesh->mVertices[i];
        vertex.normal   = mesh->mNormals[i];
        //vertex.texCoord = mesh->mTextureCoords[i];

        vertices.push_back(vertex);
    }
    vertices.shrink_to_fit();

    // - faces
    for (unsigned int i = 0; i < mesh->mNumFaces; ++i) {
        Face face = Face();

        unsigned int* it = mesh->mFaces[i].mIndices;
        std::copy(it, it + 3, face.indices.begin());

        faces.push_back(face);
    }
}

jobjectArray Mesh::jvertices(JNIEnv *env) const {
    jsize size = (jsize) vertices.size();
    jclass jcls = jnitools::findClass(env, "net/capellari/julien/opengl/Vec3");

    jobjectArray array = env->NewObjectArray(size, jcls, jnitools::construct(env, jcls, "()V"));
    for (int i = 0; i < size; ++i) {
        jobject vec = vertices[i].position.toJava(env);
        env->SetObjectArrayElement(array, i, vec);
        env->DeleteLocalRef(vec);
    }

    env->DeleteLocalRef(jcls);

    return array;
}

jobjectArray Mesh::jnormals(JNIEnv *env) const {
    jsize size = (jsize) vertices.size();
    jclass jcls = jnitools::findClass(env, "net/capellari/julien/opengl/Vec3");

    jobjectArray array = env->NewObjectArray(size, jcls, jnitools::construct(env, jcls, "()V"));
    for (int i = 0; i < size; ++i) {
        jobject vec = vertices[i].normal.toJava(env);
        env->SetObjectArrayElement(array, i, vec);
        env->DeleteLocalRef(vec);
    }

    env->DeleteLocalRef(jcls);

    return array;
}

jintArray Mesh::jindices(JNIEnv *env) const {
    jsize size = (jsize) faces.size() * 3;
    jintArray array = env->NewIntArray(size);

    jint buffer[size];
    int i = 0;

    for (auto face : faces) {
        buffer[i++] = face.indices[0];
        buffer[i++] = face.indices[1];
        buffer[i++] = face.indices[2];
    }

    env->SetIntArrayRegion(array, 0, size, buffer);

    return array;
}

// JNI Calls
extern "C" JNIEXPORT jobjectArray JNICALL
Java_net_capellari_julien_opengl_jni_JNIMesh_getVertices(JNIEnv *env, jobject jthis) {
    return jnitools::handle<Mesh>(env, jthis)->jvertices(env);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_net_capellari_julien_opengl_jni_JNIMesh_getNormals(JNIEnv *env, jobject jthis) {
    return jnitools::handle<Mesh>(env, jthis)->jnormals(env);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_net_capellari_julien_opengl_jni_JNIMesh_getIndices(JNIEnv *env, jobject jthis) {
    return jnitools::handle<Mesh>(env, jthis)->jindices(env);
}