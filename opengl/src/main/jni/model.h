//
// Created by julien on 21/11/2018.
//
#pragma once

#include <map>
#include <string>
#include <vector>

#include <assimp/Importer.hpp>
#include <assimp/scene.h>

#include "mesh.h"
#include "material.h"

class Model : public jnitools::JNIClass {
public:
    // Constructeur
    Model(std::string const& file);

    // Méthodes
    jlongArray jmeshes(JNIEnv* env) const;
    jobject jmaterials(JNIEnv* env) const;

private:
    // Attributs
    bool loaded;
    std::vector<Mesh> meshes;
    std::map<std::string,Material> materials;

    // Méthodes
    void processNode(aiNode* node, aiScene const* scene);
};
