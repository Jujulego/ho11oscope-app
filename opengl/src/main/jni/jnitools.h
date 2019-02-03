/**
 * Define tools to interact with Java code
 * @author: Julien Capellari
 */
#pragma once

#include <jni.h>
#include <string>
#include <type_traits>

#include "utils.h"

// Tools
class Args;
namespace jnitools {
    // Classe
    class JNIConvert {
    public:
        // Destructeur
        virtual ~JNIConvert() = default;

        // Méthodes
        virtual jobject toJava(JNIEnv* env) const = 0;
    };

    class JNIClass {
    public:
        // Destructeur
        virtual ~JNIClass() = default;

        // Méthodes
        virtual jlong handle() const;
    };

    // Métafonctions
    template<class O> struct is_jobject : std::enable_if<std::is_base_of<_jobject,typename std::remove_pointer<O>::type>::value> {
        using arg = O;
    };
    template<class O> struct is_jniconvert : std::enable_if<std::is_base_of<JNIConvert,O>::value> {
        using arg = O;
    };

    // Tools
    // - convertions
    template<class R> R fromJava(JNIEnv* env, jobject jobj);

    template<class R,class=void> struct jnitojava {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        explicit jnitojava(JNIEnv* env) : m_env(env) {}

        // Opérateurs
        jobject operator() (R const& obj);
    };
    template<class R> struct jnitojava<R,typename is_jniconvert<R>::type> {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        explicit jnitojava(JNIEnv* env) : m_env(env) {}

        // Opérateurs
        jobject operator() (R const& obj) {
            return reinterpret_cast<JNIConvert const*>(&obj)->toJava(m_env);
        }
    };
    template<> struct jnitojava<std::string> {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        explicit jnitojava(JNIEnv* env) : m_env(env) {}

        // Opérateurs
        jobject operator() (std::string const& obj) {
            return m_env->NewStringUTF(obj.data());
        }
    };

    template<class R> inline jobject toJava(JNIEnv* env, R const& obj) {
        return jnitojava<R>(env)(obj);
    }

    // - éléments
    jclass findClass(JNIEnv* env, jobject jobj);
    jclass findClass(JNIEnv* env, std::string const& nom);

    jfieldID findField(JNIEnv* env, jclass jcls, std::string const& nom, std::string const& type);
    jfieldID findField(JNIEnv* env, jobject jobj, std::string const& nom, std::string const& type);
    jfieldID findField(JNIEnv* env, std::string const& cls, std::string const& nom, std::string const& type);

    jmethodID findMethod(JNIEnv* env, jclass jcls, std::string const &nom, std::string const &sig);
    jmethodID findMethod(JNIEnv* env, jobject jobj, std::string const &nom, std::string const &sig);
    jmethodID findMethod(JNIEnv* env, std::string const& cls, std::string const& nom, std::string const& sig);

    // - handle
    jfieldID handleField(JNIEnv* env, jobject jobj);

    template<class T> T* handle(JNIEnv *env, jobject jobj) {
        static_assert(std::is_base_of<JNIClass,T>::value, "T should inherit from JNIClass");
        return reinterpret_cast<T*>(env->GetLongField(jobj, handleField(env, jobj)));
    }

    // - contruct
    template<class... Args> jobject construct(JNIEnv* env, jclass jcls, std::string const& sig, Args const&... args) {
        jmethodID constructor = findMethod(env, jcls, "<init>", sig);
        return env->NewObject(jcls, constructor, args...);
    }
    template<class... Args> jobject construct(JNIEnv* env, std::string const& cls, std::string const& sig, Args const&... args) {
        jclass jcls = jnitools::findClass(env, cls);
        jobject jobj = construct(env, jcls, sig, args...);
        env->DeleteLocalRef(jcls);
        return jobj;
    }

    // - call
    template<class R = jobject,class = void> struct jnimethod {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args> R operator() (Args const&... args) {
            return fromJava<R>(m_env, m_env->CallObjectMethod(m_jobj, m_jmethod, args...));
        }
    };
    template<class R> struct jnimethod<R,typename is_jobject<R>::type>{
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj) : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        R operator() (Args const&... args) {
            return (R) m_env->CallObjectMethod(m_jobj, m_jmethod, args...);
        }
    };

    #define JNIMETHOD(type, jname)                                          \
        template<> struct jnimethod<type>{                                  \
            JNIEnv* m_env;                                                  \
            jobject m_jobj;                                                 \
            jmethodID m_jmethod;                                            \
                                                                            \
            jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)         \
                    : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}       \
                                                                            \
            template<class... Args>                                         \
            typename noop<type>::arg operator() (Args const&... args) {     \
                m_env->Call##jname##Method(m_jobj, m_jmethod, args...);     \
            }                                                               \
        }

    JNIMETHOD(void,     Void);
    JNIMETHOD(jboolean, Boolean);
    JNIMETHOD(jchar,    Char);
    JNIMETHOD(jbyte,    Byte);
    JNIMETHOD(jshort,   Short);
    JNIMETHOD(jint,     Int);
    JNIMETHOD(jlong,    Long);
    JNIMETHOD(jfloat,   Float);
    JNIMETHOD(jdouble,  Double);

    template<class R, class... Args> inline R call(JNIEnv* env, jobject jobj, jmethodID jmth, Args const&... args) {
        return jnimethod<R>(env, jmth, jobj)(args...);
    }
    template<class R, class... Args> inline R call(JNIEnv* env, jobject jobj, std::string const& method, std::string const& sig, Args const&... args) {
        return jnimethod<R>(env, findMethod(env, jobj, method, sig), jobj)(args...);
    }
    template<class R, class... Args> inline R call(JNIEnv* env, jclass jcls, jobject jobj, std::string const& method, std::string const& sig, Args const&... args) {
        return jnimethod<R>(env, findMethod(env, jcls, method, sig), jobj)(args...);
    }

    // - fields
    template<class R=jobject,class=void> struct jnifield {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        R get() const {
            return fromJava<R>(m_env, m_env->GetObjectField(m_jobj, m_jfld));
        }

        void set(R const& val) {
            jobject jobj = toJava(m_env, val);
            m_env->SetObjectField(m_jobj, m_jfld, jobj);
            m_env->DeleteLocalRef(jobj);
        }
    };
    template<class R> struct jnifield<R,typename is_jobject<R>::type> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        R get() const {
            return (R) m_env->GetObjectField(m_jobj, m_jfld);
        }

        void set(R val) {
            m_env->SetObjectField(m_jobj, m_jfld, (jobject) val);
        }
    };

    #define JNIFIELD(type, jname)                                           \
        template<> struct jnifield<type> {                                  \
            JNIEnv* m_env;                                                  \
            jobject m_jobj;                                                 \
            jfieldID m_jfld;                                                \
                                                                            \
            jnifield(JNIEnv* env, jfieldID jfld, jobject jobj)              \
                    : m_env(env), m_jobj(jobj), m_jfld(jfld) {}             \
                                                                            \
            typename noop<type>::arg get() const {                          \
                return m_env->Get##jname##Field(m_jobj, m_jfld);            \
            }                                                               \
                                                                            \
            void set(typename noop<type>::arg val) {                        \
                m_env->Set##jname##Field(m_jobj, m_jfld, val);              \
            }                                                               \
        }

    JNIFIELD(jboolean, Boolean);
    JNIFIELD(jchar,    Char);
    JNIFIELD(jbyte,    Byte);
    JNIFIELD(jshort,   Short);
    JNIFIELD(jint,     Int);
    JNIFIELD(jlong,    Long);
    JNIFIELD(jfloat,   Float);
    JNIFIELD(jdouble,  Double);

    template<class R> inline R get(JNIEnv* env, jobject jobj, jfieldID jfld) {
        return jnifield<R>(env, jfld, jobj).get();
    }
    template<class R> inline R get(JNIEnv* env, jobject jobj, std::string const& nom, std::string const& type) {
        return jnifield<R>(env, findField(env, jobj, nom, type), jobj).get();
    }
    template<class R> inline R get(JNIEnv* env, jclass jcls, jobject jobj, std::string const& nom, std::string const& type) {
        return jnifield<R>(env, findField(env, jcls, nom, type), jobj).get();
    }

    template<class R> inline void set(JNIEnv* env, jobject jobj, jfieldID jfld, R const& val) {
        jnifield<R>(env, jfld, jobj).set(val);
    }
    template<class R> inline void set(JNIEnv* env, jobject jobj, std::string const& nom, std::string const& type, R const& val) {
        jnifield<R>(env, findField(env, jobj, nom, type), jobj).set(val);
    }
    template<class R> inline void set(JNIEnv* env, jclass jcls, jobject jobj, std::string const& nom, std::string const& type, R const& val) {
        jnifield<R>(env, findField(env, jcls, nom, type), jobj).set(val);
    }
}