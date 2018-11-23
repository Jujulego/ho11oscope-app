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
    template<class R> R fromJava(JNIEnv* env, jobject jobj);

    template<class R,class=void> struct jnitojava {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        jnitojava(JNIEnv* env) : m_env(env) {}

        // Opérateurs
        jobject operator() (R const& obj);
    };
    template<class R> struct jnitojava<R,typename is_jniconvert<R>::type> {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        jnitojava(JNIEnv* env) : m_env(env) {}

        // Opérateurs
        jobject operator() (R const& obj) {
            return reinterpret_cast<JNIConvert const*>(&obj)->toJava(m_env);
        }
    };
    template<> struct jnitojava<std::string> {
        // Attributs
        JNIEnv* m_env;

        // Constructeur
        jnitojava(JNIEnv* env) : m_env(env) {}

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
    template<> struct jnimethod<void>{
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jmethodID m_jmethod;

        // Constructeur
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

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
        jnimethod(JNIEnv *env, jmethodID jmethod, jobject jobj)
                : m_env(env), m_jobj(jobj), m_jmethod(jmethod) {}

        // Opérateurs
        template<class... Args>
        jdouble operator() (Args const&... args) {
            return m_env->CallDoubleMethod(m_jobj, m_jmethod, args...);
        }
    };

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
    template<> struct jnifield<jboolean> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jboolean get() const {
            return m_env->GetBooleanField(m_jobj, m_jfld);
        }

        void set(jboolean val) {
            m_env->SetBooleanField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jchar> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jchar get() const {
            return m_env->GetCharField(m_jobj, m_jfld);
        }

        void set(jchar val) {
            m_env->SetCharField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jbyte> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jbyte get() const {
            return m_env->GetByteField(m_jobj, m_jfld);
        }

        void set(jbyte val) {
            m_env->SetByteField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jshort> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jshort get() const {
            return m_env->GetShortField(m_jobj, m_jfld);
        }

        void set(jshort val) {
            m_env->SetShortField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jint> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jint get() const {
            return m_env->GetIntField(m_jobj, m_jfld);
        }

        void set(jint val) {
            m_env->SetIntField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jlong> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jlong get() const {
            return m_env->GetLongField(m_jobj, m_jfld);
        }

        void set(jlong val) {
            m_env->SetLongField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jfloat> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jfloat get() const {
            return m_env->GetFloatField(m_jobj, m_jfld);
        }

        void set(jfloat val) {
            m_env->SetFloatField(m_jobj, m_jfld, val);
        }
    };
    template<> struct jnifield<jdouble> {
        // Attributs
        JNIEnv* m_env;
        jobject m_jobj;
        jfieldID m_jfld;

        // Constructeur
        jnifield(JNIEnv* env, jfieldID jfld, jobject jobj) : m_env(env), m_jobj(jobj), m_jfld(jfld) {}

        // Méthodes
        jdouble get() const {
            return m_env->GetDoubleField(m_jobj, m_jfld);
        }

        void set(jboolean val) {
            m_env->SetDoubleField(m_jobj, m_jfld, val);
        }
    };

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