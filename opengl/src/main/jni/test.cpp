//
// Created by julie on 17/11/2018.
//
#include <jni.h>

#include "test.h"

// Méthodes
int JNITest::test() {
    return 8;
}

// JNI Calls
extern "C" JNIEXPORT jlong JNICALL
Java_net_capellari_julien_opengl_JNITest_construct(JNIEnv*, jclass) {
    return (new JNITest())->handle();
}

extern "C" JNIEXPORT jint JNICALL
Java_net_capellari_julien_opengl_JNITest_test(JNIEnv *env, jobject jthis) {
    return jnitools::handle<JNITest>(env, jthis)->test();
}