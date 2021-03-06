#
# Options générales
#
cmake_minimum_required(VERSION 3.4)
project(OpenGL)

set(CMAKE_VERBOSE_MAKEFILE 1) # Use 1 for debugging, 0 for release
set(CMAKE_CXX17_EXTENSION_COMPILE_OPTION "-std=c++17")

# Options de compilation
set(CMAKE_C_STANDARD 11)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -g3")

# Dépendances
# - Assimp
add_library(assimp SHARED IMPORTED)
set_target_properties(assimp PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libassimpd.so)

# - NDK
find_library(log-lib log)

# JNI
include_directories(src/main/jni)
include_directories(libs/include)

add_library(opengl SHARED
        src/main/jni/color.cpp
        src/main/jni/jnitools.cpp
        src/main/jni/material.cpp
        src/main/jni/mesh.cpp
        src/main/jni/model.cpp
        src/main/jni/vec.cpp
)

target_link_libraries(opengl assimp ${log-lib})