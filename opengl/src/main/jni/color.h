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
        Color() : Vec() {};
        explicit Color(aiColor3D color);
        template<class... Args> explicit Color(Args const&... args) : Vec(args...) {}

        // Méthodes
        float& r() { return this->operator[](0); }
        float& g() { return this->operator[](1); }
        float& b() { return this->operator[](2); }

        float const& r() const { return this->operator[](0); }
        float const& g() const { return this->operator[](1); }
        float const& b() const { return this->operator[](2); }

        virtual jobject toJava(JNIEnv *env) const override;
};

class ColorAlpha : public Vec<4>, public jnitools::JNIConvert {
    public:
        // Constructeurs
        ColorAlpha() : Vec() {}
        explicit ColorAlpha(aiColor4D color);
        template<class... Args> explicit ColorAlpha(Args const&... args) : Vec(args...) {}

        // Méthodes
        float& r() { return this->operator[](0); }
        float& g() { return this->operator[](1); }
        float& b() { return this->operator[](2); }
        float& a() { return this->operator[](3); }

        float const& r() const { return this->operator[](0); }
        float const& g() const { return this->operator[](1); }
        float const& b() const { return this->operator[](2); }
        float const& a() const { return this->operator[](3); }

        virtual jobject toJava(JNIEnv *env) const override;
};