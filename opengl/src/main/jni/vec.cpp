//
// Created by julie on 21/11/2018.
//

#include "vec.h"

Vec2::Vec2(aiVector2D vec) : Vec() {
    x() = vec.x;
    y() = vec.y;
}

jobject Vec2::toJava(JNIEnv *env) const {
    return jnitools::construct(env, "net/capellari/julien/opengl/Vec2", "(FF)V", x(), y());
}

template<> Vec2 jnitools::fromJava<Vec2>(JNIEnv* env, jobject jobj) {
    Vec2 r;

    r.x() = call<jfloat>(env, jobj, "getX", "()F");
    r.y() = call<jfloat>(env, jobj, "getY", "()F");

    return r;
}

Vec3::Vec3(aiVector3D vec) : Vec() {
    x() = vec.x;
    y() = vec.y;
    z() = vec.z;
}

jobject Vec3::toJava(JNIEnv *env) const {
    return jnitools::construct(env, "net/capellari/julien/opengl/Vec3", "(FFF)V", x(), y(), z());
}

template<> Vec3 jnitools::fromJava<Vec3>(JNIEnv* env, jobject jobj) {
    Vec3 r;

    r.x() = call<jfloat>(env, jobj, "getX", "()F");
    r.y() = call<jfloat>(env, jobj, "getY", "()F");
    r.z() = call<jfloat>(env, jobj, "getZ", "()F");

    return r;
}

jobject Vec4::toJava(JNIEnv *env) const {
    return jnitools::construct(env, "net/capellari/julien/opengl/Vec4", "(FFFF)V", x(), y(), z(), a());
}

template<> Vec4 jnitools::fromJava<Vec4>(JNIEnv *env, jobject jobj) {
    Vec4 r;

    r.x() = call<jfloat>(env, jobj, "getX", "()F");
    r.y() = call<jfloat>(env, jobj, "getY", "()F");
    r.z() = call<jfloat>(env, jobj, "getZ", "()F");
    r.a() = call<jfloat>(env, jobj, "getA", "()F");

    return r;
}