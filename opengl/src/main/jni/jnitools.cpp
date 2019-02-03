//
// Created by julien on 17/11/2018.
//
#include "jnitools.h"

// Namespaces
using namespace jnitools;

// Tools
jclass jnitools::findClass(JNIEnv *env, jobject jobj) {
    return env->GetObjectClass(jobj);
}

jclass jnitools::findClass(JNIEnv *env, std::string const& nom) {
    return env->FindClass(nom.data());
}

jfieldID jnitools::findField(JNIEnv *env, jclass jcls, std::string const &nom, std::string const &type) {
    return env->GetFieldID(jcls, nom.data(), type.data());
}

jfieldID jnitools::findField(JNIEnv *env, jobject jobj, std::string const &nom, std::string const &type) {
    jclass jcls = findClass(env, jobj);
    jfieldID jfld = env->GetFieldID(jcls, nom.data(), type.data());
    env->DeleteLocalRef(jcls);
    return jfld;
}

jfieldID jnitools::findField(JNIEnv *env, std::string const& cls, std::string const &nom, std::string const &type) {
    jclass jcls = findClass(env, cls);
    jfieldID jfld = env->GetFieldID(jcls, nom.data(), type.data());
    env->DeleteLocalRef(jcls);
    return jfld;
}

jmethodID jnitools::findMethod(JNIEnv *env, jclass jcls, std::string const &nom, std::string const &sig) {
    return env->GetMethodID(jcls, nom.data(), sig.data());
}

jmethodID jnitools::findMethod(JNIEnv *env, jobject jobj, std::string const& nom, std::string const& sig) {
    jclass jcls = findClass(env, jobj);
    jmethodID jmth = env->GetMethodID(jcls, nom.data(), sig.data());
    env->DeleteLocalRef(jcls);
    return jmth;
}

jmethodID jnitools::findMethod(JNIEnv *env, std::string const& cls, std::string const& nom, std::string const& sig) {
    jclass jcls = findClass(env, cls);
    jmethodID jmth = env->GetMethodID(jcls, nom.data(), sig.data());
    env->DeleteLocalRef(jcls);
    return jmth;
}

jfieldID jnitools::handleField(JNIEnv* env, jobject jobj) {
    return jnitools::findField(env, jobj, "nativeHandle", "J");
}

jlong JNIClass::handle() const {
    return reinterpret_cast<jlong>(this);
}

template<> std::string jnitools::fromJava<std::string>(JNIEnv* env, jobject jobj) {
    if (!jobj) return "";

    char const* str = env->GetStringUTFChars((jstring) jobj, nullptr);
    std::string ret(str);

    env->ReleaseStringUTFChars((jstring) jobj, str);

    return ret;
}

// JNI Calls
extern "C" JNIEXPORT void JNICALL
Java_net_capellari_julien_opengl_base_JNIClass_dispose(JNIEnv *env, jobject jthis) {
    delete handle<JNIClass>(env, jthis);
}