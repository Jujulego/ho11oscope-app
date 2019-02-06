#version 320 es
#extension GL_EXT_shader_io_blocks : enable

// Uniforms
layout (std140) uniform Matrices {
    mat4 mvpMatrix;
    mat4 modelMatrix;
    mat4 lightMatrix;
};

layout (std140) uniform Stables {
    mat4 viewMatrix;
    mat4 projMatrix;

    vec3 lightPosition; // in light space
};

// Entrées
in vec3 aPosition;
in vec3 aNormal;
in vec2 aTexCoord;

// Sorties
out Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 normal;

    // - textures
    vec2 uv;
} vecs;

void main() {
    // vertex positions
    gl_Position = mvpMatrix * vec4(aPosition, 1);
    vecs.uv = aTexCoord;

    // Compute world space positions
    vecs.position = (modelMatrix * vec4(aPosition, 1)).xyz;

    // Compute camera space normals
    vecs.normal = (viewMatrix * modelMatrix * vec4(aNormal, 0)).xyz;

    // Vector from vertex to camera
    vec3 vertexCamera = (viewMatrix * modelMatrix * vec4(aPosition, 1)).xyz;
    vecs.eyeDirection = vec3(0, 0, 0) - vertexCamera;
}