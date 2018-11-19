//
// Created by julien on 17/11/2018.
//
#include "jnitools.h"

// Namespaces
using namespace jnitools;

// Méthodes
jlong JNIClass::handle() const {
    return reinterpret_cast<jlong>(this);
}

// Tools
jfieldID jnitools::handleField(JNIEnv* env, jobject jobj) {
    jclass c = env->GetObjectClass(jobj);
    return env->GetFieldID(c, "nativeHandle", "J");
}

jclass jnitools::findClass(JNIEnv *env, std::string const& nom) {
    return env->FindClass(nom.data());
}

jmethodID jnitools::findMethod(JNIEnv *env, jclass jcls, std::string const &nom,
                               std::string const &sig) {
    return env->GetMethodID(jcls, nom.data(), sig.data());
}

jmethodID jnitools::findMethod(JNIEnv *env, std::string const& cls, std::string const& nom, std::string const& sig) {
    return env->GetMethodID(findClass(env, cls), nom.data(), sig.data());
}

// JNI Calls
extern "C" JNIEXPORT void JNICALL
Java_net_capellari_julien_opengl_base_JNIClass_dispose(JNIEnv *env, jobject jthis) {
    delete handle<JNIClass>(env, jthis);
}