//
// Created by julie on 03/02/2019.
//
#pragma once

#include <assimp/types.h>

#include "jnitools.h"
#include "vec.h"

class Color : public Vec<3>, public jnitools::JNIConvert {
    public:
        // Constructeurs
        Color() = default;
        Color(aiColor3D color);
        template<class... Args> Color(Args const&... args) : Vec(args...) {}

        // Méthodes
        float& r() { return this->operator[](0); }
        float& g() { return this->operator[](1); }
        float& b() { return this->operator[](2); }

        float const& r() const { return this->operator[](0); }
        float const& g() const { return this->operator[](1); }
        float const& b() const { return this->operator[](2); }

        virtual jobject toJava(JNIEnv *env) const override;
};