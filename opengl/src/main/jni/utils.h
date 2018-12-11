//
// Created by julie on 11/12/2018.
//
#pragma once

#include <string>

inline std::string parent_path(std::string const& path) {
    size_t slash = path.rfind('/');

    if (slash != std::string::npos) {
        return path.substr(0, slash + 1);
    } else {
        return path;
    }
}