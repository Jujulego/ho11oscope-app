//
// Created by julien on 17/11/2018.
//
#pragma once

#include <vector>
#include <list>

#include <assimp/mesh.h>

#include "jnitools.h"
#include "vec.h"

class Mesh : public jnitools::JNIClass {
public:
    // Structures
    struct Vertex {
        // - géométrie
        Vec3 position;
        Vec3 normal;

        // - rendu
        Vec2 texCoord;
    };

    struct Face {
        std::array<unsigned int,3> indices;
    };

    // Constructeur
    Mesh();
    Mesh(aiMesh* mesh);

    // Méthodes
    jobjectArray jvertices(JNIEnv* env) const;
    jobjectArray jnormals(JNIEnv* env) const;
    jintArray jindices(JNIEnv* env) const;

private:
    // Attributs
    unsigned int material;
    std::list<Face> faces;
    std::vector<Vertex> vertices;
};
