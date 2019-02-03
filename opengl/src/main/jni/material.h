//
// Created by julien on 21/11/2018.
//
#pragma once

#include <list>
#include <string>

#include <assimp/material.h>

#include "color.h"
#include "jnitools.h"
#include "vec.h"

class Material : public jnitools::JNIConvert {
public:
    // Structure
    struct Texture {
        // - general
        aiTextureType type;
        std::string file;
        int uv_chanel;

        // - blend
        aiTextureOp blend_op;
        float blend_factor;
    };

    // Constructeur
    Material() = default;
    Material(aiMaterial* material, std::string const& dossier);

    // Méthodes
    std::string const& name() const;

    virtual jobject toJava(JNIEnv *env) const override;

private:
    // Attributs
    std::string m_name;
    float m_specularExp;
    float m_opacity;

    // - couleurs
    Color m_ambientColor;
    Color m_diffuseColor;
    Color m_specularColor;

    // - textures
    std::list<Texture> m_textures;
};
