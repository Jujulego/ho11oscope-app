//
// Created by julie on 17/11/2018.
//
#pragma once

#include <algorithm>
#include <array>
#include <jni.h>
#include <ostream>

#include "jnitools.h"

template<size_t SIZE>
class Vec {
    // Static checks
    static_assert(SIZE >= 2, "SIZE should be at least 2");

    public:
        // Alias
        using value_type      = float;
        using reference       = float&;
        using const_reference = float const&;
        using pointer         = float*;
        using const_pointer   = float const*;
        using iterator        = typename std::array<float,SIZE>::iterator;
        using const_iterator  = typename std::array<float,SIZE>::const_iterator;
        using size_type       = size_t;
        using difference_type = std::ptrdiff_t;

        // Constructeur
        Vec() {}

        template<size_t S2>
        Vec(Vec<S2> const& o) : m_data(o.m_data) {
            static_assert(S2 >= SIZE, "S2 should be as great as size");
        }

        template<size_t S2, class... Args>
        Vec(Vec<S2> const& o, Args... v) {
            static_assert((S2 + sizeof...(Args)) == SIZE, "Too many args !");

            for (size_t i = 0; i < S2; ++i) {
                m_data[i] = o[i];
            }

            auto args = {v...};
            for (size_t i = S2; i < (S2 + sizeof...(Args)); ++i) {
                m_data[i] = args[i];
            }
        }

        template<class... Args>
        Vec(Args... v) : m_data({v...}) {
            static_assert(sizeof...(Args) == SIZE, "Too many args !");
        }

        // Opérateurs
        explicit operator bool () const {
            return std::any_of(begin(), end(), [] (float v) { return v != 0; });
        }

        bool operator ! () const {
            return !std::any_of(begin(), end(), [] (float v) { return v != 0; });
        }

        // - accès
        reference operator [] (size_type i) {
            return m_data[i];
        }

        const_reference operator [] (size_type i) const {
            return m_data[i];
        }

        // - comparaison
        bool operator == (Vec const& v) const {
            return std::equal(begin(), end(), v.begin());
        }

        bool operator != (Vec const& v) const {
            return !std::equal(begin(), end(), v.begin());
        }

        // - arithmétique
        Vec operator + () const {
            Vec r;

            for (size_type i = 0; i < SIZE; ++i) {
                r[i] = +m_data[i];
            }

            return r;
        }

        Vec operator - () const {
            Vec r;

            for (size_type i = 0; i < SIZE; ++i) {
                r[i] = -m_data[i];
            }

            return r;
        }

        Vec& operator += (Vec const& v) {
            for (size_type i = 0; i < SIZE; ++i) {
                m_data[i] += v[i];
            }

            return *this;
        }

        Vec& operator -= (Vec const& v) {
            for (size_type i = 0; i < SIZE; ++i) {
                m_data[i] -= v[i];
            }

            return *this;
        }

        Vec& operator *= (float const& v) {
            for (size_type i = 0; i < SIZE; ++i) {
                m_data[i] *= v;
            }

            return *this;
        }

        Vec& operator /= (float const& v) {
            for (size_type i = 0; i < SIZE; ++i) {
                m_data[i] /= v;
            }

            return *this;
        }

        // Méthodes
        size_type size() const {
            return SIZE;
        }

        // - itérateurs
        iterator begin() { return m_data.begin(); }
        iterator end()   { return m_data.end();   }

        const_iterator begin() const { return m_data.cbegin(); }
        const_iterator end()   const { return m_data.cend();   }

    private:
        // Attributs
        std::array<float,SIZE> m_data;
};

// Opérateurs externes
template<size_t size>
Vec<size> operator + (Vec<size> const& v1, Vec<size> const& v2) {
    Vec<size> r;

    for (size_t i = 0; i < size; ++i) {
        r[i] = v1[i] + v2[i];
    }

    return r;
}

template<size_t size>
Vec<size> operator - (Vec<size> const& v1, Vec<size> const& v2) {
    Vec<size> r;

    for (size_t i = 0; i < size; ++i) {
        r[i] = v1[i] - v2[i];
    }

    return r;
}

template<size_t size>
Vec<size> operator * (Vec<size> const& v, float const& f) {
    v *= f;
    return v;
}

template<size_t size>
Vec<size> operator * (float const& f, Vec<size> const& v) {
    v *= f;
    return v;
}

template<size_t size>
Vec<size> operator / (Vec<size> const& v, float const& f) {
    v /= f;
    return v;
}

template<size_t size>
Vec<size> operator / (float const& f, Vec<size> const& v) {
    v /= f;
    return v;
}

// - affichage
template<size_t size>
std::ostream& operator << (std::ostream& stream, Vec<size> const& v) {
    stream << "(";

    for (size_t i = 0; i < size; ++i) {
        stream << v[i];
        if (i != size - 1) stream << ",";
    }

    return stream << ")";
}

// Classe
class Vec2 : Vec<2>, jnitools::JNIConvert {
    public:
        // Méthodes
        float& x() { return this->operator[](0); }
        float& y() { return this->operator[](1); }

        float const& x() const { return this->operator[](0); }
        float const& y() const { return this->operator[](1); }

        virtual jobject toJava(JNIEnv *env) const override {
            return jnitools::construct(env, "net/capellari/julien/opengl/Vec2", "(FF)V", x(), y());
        }
};

class Vec3 : Vec<3>, jnitools::JNIConvert {
    public:
        // Méthodes
        float& x() { return this->operator[](0); }
        float& y() { return this->operator[](1); }
        float& z() { return this->operator[](2); }

        float const& x() const { return this->operator[](0); }
        float const& y() const { return this->operator[](1); }
        float const& z() const { return this->operator[](2); }

        virtual jobject toJava(JNIEnv *env) const override {
            return jnitools::construct(env, "net/capellari/julien/opengl/Vec3", "(FFF)V", x(), y(), z());
        }
};

class Vec4 : Vec<4>, jnitools::JNIConvert {
    public:
        // Méthodes
        float& x() { return this->operator[](0); }
        float& y() { return this->operator[](1); }
        float& z() { return this->operator[](2); }
        float& a() { return this->operator[](3); }

        float const& x() const { return this->operator[](0); }
        float const& y() const { return this->operator[](1); }
        float const& z() const { return this->operator[](2); }
        float const& a() const { return this->operator[](3); }

        virtual jobject toJava(JNIEnv *env) const override {
            return jnitools::construct(env, "net/capellari/julien/opengl/Vec4", "(FFFF)V", x(), y(), z(), a());
        }
};

namespace jnitools {
    template<> Vec2 fromJava<Vec2>(JNIEnv* env, jobject jobj) {
        Vec2 r;

        r.x() = call<jfloat>(env, "net/capellari/julien/opengl/Vec2", "getX", "()F", jobj);
        r.y() = call<jfloat>(env, "net/capellari/julien/opengl/Vec2", "getY", "()F", jobj);

        return r;
    }
    template<> Vec3 fromJava<Vec3>(JNIEnv* env, jobject jobj) {
        Vec3 r;

        r.x() = call<jfloat>(env, "net/capellari/julien/opengl/Vec3", "getX", "()F", jobj);
        r.y() = call<jfloat>(env, "net/capellari/julien/opengl/Vec3", "getY", "()F", jobj);
        r.z() = call<jfloat>(env, "net/capellari/julien/opengl/Vec3", "getZ", "()F", jobj);

        return r;
    }
    template<> Vec4 fromJava<Vec4>(JNIEnv* env, jobject jobj) {
        Vec4 r;

        r.x() = call<jfloat>(env, "net/capellari/julien/opengl/Vec4", "getX", "()F", jobj);
        r.y() = call<jfloat>(env, "net/capellari/julien/opengl/Vec4", "getY", "()F", jobj);
        r.z() = call<jfloat>(env, "net/capellari/julien/opengl/Vec4", "getZ", "()F", jobj);
        r.a() = call<jfloat>(env, "net/capellari/julien/opengl/Vec4", "getA", "()F", jobj);

        return r;
    }
}