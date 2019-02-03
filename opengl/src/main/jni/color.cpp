//
// Created by julie on 03/02/2019.
//

#include "color.h"

Color::Color(aiColor3D color) {
    r() = color.r;
    g() = color.g;
    b() = color.b;
}

jobject Color::toJava(JNIEnv *env) const {
    return jnitools::construct(env, "net/capellari/julien/opengl/Color", "(FFF)V", r(), g(), b());
}

template<> Color jnitools::fromJava<Color>(JNIEnv* env, jobject jobj) {
    Color r;

    r.r() = call<jfloat>(env, jobj, "getR", "()F");
    r.g() = call<jfloat>(env, jobj, "getG", "()F");
    r.b() = call<jfloat>(env, jobj, "getB", "()F");

    return r;
}