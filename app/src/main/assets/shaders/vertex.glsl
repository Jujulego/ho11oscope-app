// Uniformes
// - light
uniform vec3 uLight; // in world space

// - matrices
uniform mat4 uMVP;
uniform mat4 uM;
uniform mat4 uV;

// Attributs
attribute vec3 aPosition;
attribute vec3 aNormal;
attribute vec3 aAmbientColor;
attribute vec3 aDiffuseColor;
attribute vec3 aSpecularColor;
attribute float aSpecularExp;
attribute float aOpacity;

// Communes au shaders
// - world space
varying vec3 position;

// - camera space
varying vec3 eyeDirection;
varying vec3 lightDirection;
varying vec3 normal;

// - couleurs
varying vec3 ambientColor;
varying vec3 diffuseColor;
varying vec3 specularColor;
varying float specularExp;
varying float opacity;

void main() {
    // vertex position
    gl_Position = uMVP * vec4(aPosition, 1);

    // Transmit color to fragment shader
    ambientColor = aAmbientColor;
    diffuseColor = aDiffuseColor;
    specularColor = aSpecularColor;
    specularExp = aSpecularExp;
    opacity = aOpacity;

    // Compute world space position
    position = (uM * vec4(aPosition, 1)).xyz;

    // Compute camera space normal
    normal = (uV * uM * vec4(aNormal, 1)).xyz;

    // Vector from vertex to camera
    vec3 vertexCamera = (uV * uM * vec4(aPosition, 1)).xyz;
    eyeDirection = vec3(0, 0, 0) - vertexCamera;

    // Vector from vertex to light
    vec3 lightCamera = (uV * vec4(uLight, 1)).xyz;
    lightDirection = lightCamera + eyeDirection;
}