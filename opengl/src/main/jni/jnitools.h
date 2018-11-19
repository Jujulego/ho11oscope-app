//
// Created by julien on 17/11/2018.
//
#pragma once

#include <jni.h>
#include <string>
#include <type_traits>

// Tools
namespace jnitools {
    // Classe
    struct JNIClass {
        // Méthodes
        jlong handle() const;
    };

    class JNIConvert {
        public:
            // Destructeur
            virtual ~JNIConvert() = default;

            // Méthodes
            virtual jobject toJava(JNIEnv* env) const = 0;
    };

    template<class R> R fromJava(JNIEnv* env, jobject jobj);

    // Tools
    jclass findClass(JNIEnv* env, std::string const& nom);
    jmethodID findMethod(JNIEnv *env, jclass jcls, std::string const &nom, std::string const &sig);
    jmethodID findMethod(JNIEnv* env, std::string const& cls, std::string const& nom, std::string const& sig);

    // - handle
    jfieldID handleField(JNIEnv* env, jobject jobj);

    template<class T>
    T* handle(JNIEnv *env, jobject jobj) {
        static_assert(std::is_base_of<JNIClass,T>::value, "T should inherit from JNIClass");
        return reinterpret_cast<T*>(env->GetLongField(jobj, handleField(env, jobj)));
    }

    // - contruct
    template<class... Args>
    jobject construct(JNIEnv* env, jclass jcls, std::string const& sig, Args const&... args) {
        jmethodID constructor = findMethod(env, jcls, "<init>", sig);
        return env->NewObject(jcls, constructor, args...);
    }
    template<class... Args>
    jobject construct(JNIEnv* env, std::string const& cls, std::string const& sig, Args const&... args) {
        return construct(env, jnitools::findClass(env, cls), sig, args...);
    }

    // - call
    template<class R = jobject> struct jnimethod {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        R operator() (Args const&... args) {
            return fromJava(m_env, m_env->CallObjectMethod(m_jobj, m_jmethod, args...));
        }
    };
    template<> struct jnimethod<jobject>{
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jobject operator() (Args const&... args) {
            return m_env->CallObjectMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<void>{
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        void operator() (Args const&... args) {
            m_env->CallVoidMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jboolean> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jboolean operator() (Args const&... args) {
            return m_env->CallBooleanMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jchar> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jchar operator() (Args const&... args) {
            return m_env->CallCharMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jbyte> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jbyte operator() (Args const&... args) {
            return m_env->CallByteMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jshort> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jshort operator() (Args const&... args) {
            return m_env->CallShortMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jint> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jint operator() (Args const&... args) {
            return m_env->CallIntMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jlong> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jlong operator() (Args const&... args) {
            return m_env->CallLongMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jfloat> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jfloat operator() (Args const&... args) {
            return m_env->CallFloatMethod(m_jobj, m_jmethod, args...);
        }
    };
    template<> struct jnimethod<jdouble> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv* env, jobject jobj, jmethodID jmethod) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jdouble operator() (Args const&... args) {
            return m_env->CallDoubleMethod(m_jobj, m_jmethod, args...);
        }
    };

    template<class R, class... Args>
    inline auto call(JNIEnv* env, jmethodID jmethod, jobject jobj, Args const&... args) {
        return jnimethod<R>(env, jobj, jmethod)(args...);
    }

    template<class R, class... Args>
    inline auto call(JNIEnv* env, jclass jcls, std::string const& method, std::string const& sig, jobject jobj, Args const&... args) {
        return jnimethod<R>(env, jobj, findMethod(env, jcls, method, sig))(args...);
    }

    template<class R, class... Args>
    inline auto call(JNIEnv* env, std::string const& cls, std::string const& method, std::string const& sig, jobject jobj, Args const&... args) {
        jclass jcls = findClass(env, cls);
        return jnimethod<R>(env, jobj, findMethod(env, jcls, method, sig))(args...);
    }
}