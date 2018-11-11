#version 310 es

// Uniforms
layout (std140) uniform Matrices {
    mat4 mvpMatrix;
    mat4 modelMatrix;
};

layout (std140) uniform Stables {
    mat4 viewMatrix;
    mat4 projMatrix;

    vec3 lightPosition; // in world space
};

// Entrées
in vec3 aPosition;
in vec3 aNormal;
in vec3 aAmbientColor;
in vec3 aDiffuseColor;
in vec3 aSpecularColor;
in float aSpecularExp;
in float aOpacity;

// Sorties
out Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;
} vecs;

// - couleurs
out Material {
    vec3 ambientColor;
    vec3 diffuseColor;
    vec3 specularColor;
    float specularExp;
    float opacity;
} material;

void main() {
    // vertex positions
    gl_Position = mvpMatrix * vec4(aPosition, 1);

    // Transmit color to fragment shader
    material.ambientColor  = aAmbientColor;
    material.diffuseColor  = aDiffuseColor;
    material.specularColor = aSpecularColor;
    material.specularExp   = aSpecularExp;
    material.opacity       = aOpacity;

    // Compute world space positions
    vecs.position = (modelMatrix * vec4(aPosition, 1)).xyz;

    // Compute camera space normals
    vecs.normal = (viewMatrix * modelMatrix * vec4(aNormal, 0)).xyz;

    // Vector from vertex to camera
    vec3 vertexCamera = (viewMatrix * modelMatrix * vec4(aPosition, 1)).xyz;
    vecs.eyeDirection = vec3(0, 0, 0) - vertexCamera;

    // Vector from vertex to light
    vec3 lightCamera = (viewMatrix * vec4(lightPosition, 1)).xyz;
    vecs.lightDirection = lightCamera + vecs.eyeDirection;
}