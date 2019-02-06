//
// Created by julie on 03/02/2019.
//

#include "color.h"

Color::Color(aiColor3D color) : Vec() {
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

ColorAlpha::ColorAlpha(aiColor4D color) : Vec() {
    r() = color.r;
    g() = color.g;
    b() = color.b;
    a() = color.a;
}

jobject ColorAlpha::toJava(JNIEnv *env) const {
    return jnitools::construct(env, "net/capellari/julien/opengl/ColorAlpha", "(FFFF)V", r(), g(), b(), a());
}

template<> ColorAlpha jnitools::fromJava<ColorAlpha>(JNIEnv* env, jobject jobj) {
    ColorAlpha r;

    r.r() = call<jfloat>(env, jobj, "getR", "()F");
    r.g() = call<jfloat>(env, jobj, "getG", "()F");
    r.b() = call<jfloat>(env, jobj, "getB", "()F");
    r.a() = call<jfloat>(env, jobj, "getA", "()F");

    return r;
}