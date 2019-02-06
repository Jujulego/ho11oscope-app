//
// Created by julien on 21/11/2018.
//
#include "material.h"

#include <android/log.h>

Material::Material(aiMaterial* material, std::string const& dossier) {
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
    m_ambientColor = Color(buf);

    material->Get(AI_MATKEY_COLOR_DIFFUSE, buf);
    m_diffuseColor = Color(buf);

    material->Get(AI_MATKEY_COLOR_SPECULAR, buf);
    m_specularColor = Color(buf);

    // textures
    for (aiTextureType const& type : {aiTextureType_DIFFUSE}) {
        unsigned int i = 0;

        while (true) {
            Texture tex = Texture();

            // General parameters
            tex.type = type;

            if (material->Get(AI_MATKEY_TEXTURE(type, i), str) == aiReturn_FAILURE) break;
            tex.file = dossier + std::string(str.C_Str());

            material->Get(AI_MATKEY_UVWSRC(type, i), tex.uv_chanel);

            // Blend parameters
            material->Get(AI_MATKEY_TEXOP(type, i), tex.blend_op);
            material->Get(AI_MATKEY_TEXBLEND(type, i), tex.blend_factor);

            // Debug !
            __android_log_print(ANDROID_LOG_DEBUG, "Material", "Texture n°%u : %s (channel %d, blend %d %f)", i, tex.file.data(), tex.uv_chanel, tex.blend_op, tex.blend_factor);
            m_textures.push_back(tex);

            i++;
        }
    }
}

jobject Material::toJava(JNIEnv* env) const {
    jstring jstr = (jstring) jnitools::toJava(env, m_name);
    jobject jobj = jnitools::construct(env, "net/capellari/julien/opengl/Material", "(Ljava/lang/String;)V", jstr);
    env->DeleteLocalRef(jstr);

    jnitools::set<Color>(env, jobj, "ambientColor",  "Lnet/capellari/julien/opengl/Color;", m_ambientColor);
    jnitools::set<Color>(env, jobj, "diffuseColor",  "Lnet/capellari/julien/opengl/Color;", m_diffuseColor);
    jnitools::set<Color>(env, jobj, "specularColor", "Lnet/capellari/julien/opengl/Color;", m_specularColor);

    jnitools::set<jfloat>(env, jobj, "specularExp", "F", m_specularExp);
    jnitools::set<jfloat>(env, jobj, "opacity",     "F", m_opacity);

    if (!m_textures.empty()) {
        jnitools::set<std::string>(env, jobj, "texture", "Ljava/lang/String;", m_textures.front().file);
    }

    return jobj;
}

std::string const &Material::name() const {
    return m_name;
}
