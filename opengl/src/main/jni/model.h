//
// Created by julien on 21/11/2018.
//
#pragma once

#include <string>
#include <vector>

#include <assimp/Importer.hpp>
#include <assimp/scene.h>

#include "material.h"

class Mesh;
class Model : public jnitools::JNIClass {
public:
    // Constructeur
    Model(std::string const& file);

    // Méthodes
    Material getMaterial(unsigned int i) const;

    jlongArray jmeshes(JNIEnv* env) const;
    jobject jmaterials(JNIEnv* env) const;

private:
    // Attributs
    bool loaded;
    std::vector<Mesh> meshes;
    std::vector<Material> materials;

    // Méthodes
    void processNode(aiNode* node, aiScene const* scene);
};

#include "mesh.h"