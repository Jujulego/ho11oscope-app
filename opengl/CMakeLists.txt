cmake_minimum_required(VERSION 3.4)

#
# Options générales
#
project(OpenGL)

set(CMAKE_VERBOSE_MAKEFILE 1) # Use 1 for debugging, 0 for release
set(CMAKE_CXX17_EXTENSION_COMPILE_OPTION "-std=c++17")

#
# Options de compilation
#
set(CMAKE_C_STANDARD 11)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -g3")

#
# Sources
#
set(SOURCE_FILES
    # JNI
    src/main/jni/jnitools.cpp
    src/main/jni/mesh.cpp
    src/main/jni/test.cpp
)

include_directories(libs/include)
include_directories(src/main/jni)

#
# Librairie
#
add_library(opengl SHARED ${SOURCE_FILES})

target_link_libraries(opengl libs/libassimp.so)