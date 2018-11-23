//
// Created by julien on 21/11/2018.
//
#include "material.h"

Material::Material(aiMaterial* material) {
    // nom
    aiString str;
    material->Get(AI_MATKEY_NAME, str);
    m_name = str.C_Str();

    // valeurs
    material->Get(AI_MATKEY_SHININESS, m_specularExp);
    material->Get(AI_MATKEY_OPACITY,   m_opacity);

    // couleurs
    aiColor3D buf;
    material->Get(AI_MATKEY_COLOR_AMBIENT, buf);
    m_ambientColor = buf;

    material->Get(AI_MATKEY_COLOR_DIFFUSE, buf);
    m_diffuseColor = buf;

    material->Get(AI_MATKEY_COLOR_SPECULAR, buf);
    m_specularColor = buf;
}

jobject Material::toJava(JNIEnv* env) const {
    jstring jstr = (jstring) jnitools::toJava(env, m_name);
    jobject jobj = jnitools::construct(env, "net/capellari/julien/opengl/Material", "(Ljava/lang/String;)V", jstr);
    env->DeleteLocalRef(jstr);

    jnitools::set<Vec3>(env, jobj, "ambientColor",  "net/capellari/julien/opengl/Vec3", m_ambientColor);
    jnitools::set<Vec3>(env, jobj, "diffuseColor",  "net/capellari/julien/opengl/Vec3", m_diffuseColor);
    jnitools::set<Vec3>(env, jobj, "specularColor", "net/capellari/julien/opengl/Vec3", m_specularColor);

    jnitools::set<jfloat>(env, jobj, "specularExp", "F", m_specularExp);
    jnitools::set<jfloat>(env, jobj, "opacity",     "F", m_opacity);

    return jobj;
}

std::string const &Material::name() const {
    return m_name;
}
