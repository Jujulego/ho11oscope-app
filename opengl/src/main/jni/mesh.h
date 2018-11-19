//
// Created by julie on 17/11/2018.
//
#pragma once

#include <vector>

#include "jnitools.h"
#include "vec.h"

class Mesh : public jnitools::JNIClass {
    private:
        // Attributs
        std::vector<Vec3> vertices;
        std::vector<Vec3> normals;
};
