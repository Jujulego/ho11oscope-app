//
// Created by julien on 21/11/2018.
//
#include "model.h"

#include <assimp/postprocess.h>
#include <android/log.h>

#include "utils.h"

Model::Model(std::string const& file) {
    // Initialisation Assimp
    Assimp::Importer importer;
    importer.SetPropertyBool(AI_CONFIG_PP_PTV_NORMALIZE, true);

    const aiScene* scene = importer.ReadFile(file, aiProcess_JoinIdenticalVertices
            | aiProcess_Triangulate
            | aiProcess_GenSmoothNormals
            | aiProcess_PreTransformVertices
            | aiProcess_FlipUVs
            | aiProcess_GenUVCoords
            | aiProcess_TransformUVCoords
            | aiProcess_RemoveRedundantMaterials
            | aiProcess_OptimizeMeshes
    );

    // Check errors
    if (!scene) {
        __android_log_print(ANDROID_LOG_ERROR, "Model", "While importing %s : %s", file.data(), importer.GetErrorString());
        return;
    }

    // Récupération des données
    std::string dossier = parent_path(file);
    materials.reserve(scene->mNumMaterials);
    for (unsigned int i = 0; i < scene->mNumMaterials; ++i) {
        Material mat(scene->mMaterials[i], dossier);
        materials.push_back(mat);
    }

    processNode(scene->mRootNode, scene);

    loaded = true;
    __android_log_print(ANDROID_LOG_DEBUG, "Model", "%s loaded !", file.data());
}

Material Model::getMaterial(unsigned int i) const {
    return materials[i];
}

void Model::processNode(aiNode *node, aiScene const* scene) {
    meshes.reserve(meshes.size() + node->mNumMeshes);
    for (unsigned i = 0; i < node->mNumMeshes; ++i) {
        Mesh mesh(*this, scene->mMeshes[node->mMeshes[i]]);
        meshes.push_back(mesh);
    }

    for (unsigned i = 0; i < node->mNumChildren; ++i) {
        processNode(node->mChildren[i], scene);
    }
}

jlongArray Model::jmeshes(JNIEnv *env) const {
    jsize size = (jsize) meshes.size();
    jlongArray array = env->NewLongArray(size);

    jlong handles[size];
    for (int i = 0; i < size; ++i) {
        handles[i] = meshes[i].handle();
    }

    env->SetLongArrayRegion(array, 0, size, handles);

    return array;
}

jobject Model::jmaterials(JNIEnv *env) const {
    jclass jcls = jnitools::findClass(env, "java/util/HashMap");
    jmethodID jput = jnitools::findMethod(env, jcls, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jobject jmap = jnitools::construct(env, jcls, "(I)V", materials.size());

    for (auto mat : materials) {
        jstring jkey = (jstring) jnitools::toJava(env, mat.name());
        jobject jval = mat.toJava(env);

        jnitools::call<jobject>(env, jmap, jput, jkey, jval);

        env->DeleteLocalRef(jkey);
        env->DeleteLocalRef(jval);
    }

    env->DeleteLocalRef(jcls);

    return jmap;
}

// JNI calls
extern "C" JNIEXPORT jlong JNICALL
Java_net_capellari_julien_opengl_jni_Model_construct(JNIEnv *env, jclass, jstring file) {
    std::string str = jnitools::fromJava<std::string>(env, file);
    __android_log_print(ANDROID_LOG_DEBUG, "Model", "Loading %s ...", str.data());
    return (new Model(str))->handle();
}

extern "C" JNIEXPORT jobject JNICALL
Java_net_capellari_julien_opengl_jni_Model_getMaterials(JNIEnv *env, jobject jthis) {
    return jnitools::handle<Model>(env, jthis)->jmaterials(env);
}

extern "C" JNIEXPORT jlongArray JNICALL
Java_net_capellari_julien_opengl_jni_Model_nativeMeshes(JNIEnv *env, jobject jthis) {
    return jnitools::handle<Model>(env, jthis)->jmeshes(env);
}