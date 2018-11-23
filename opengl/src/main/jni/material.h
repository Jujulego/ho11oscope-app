//
// Created by julien on 21/11/2018.
//
#pragma once

#include <string>

#include <assimp/material.h>

#include "jnitools.h"
#include "vec.h"

class Material : public jnitools::JNIConvert {
public:
    // Constructeur
    Material() = default;
    Material(aiMaterial* material);

    // Méthodes
    std::string const& name() const;

    virtual jobject toJava(JNIEnv *env) const override;

private:
    // Attributs
    std::string m_name;
    float m_specularExp;
    float m_opacity;

    // - couleurs
    Vec3 m_ambientColor;
    Vec3 m_diffuseColor;
    Vec3 m_specularColor;
};
