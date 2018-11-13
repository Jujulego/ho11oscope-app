#version 310 es
precision mediump float;

// Structures
struct Material {
    vec3 ambientColor;
    vec3 diffuseColor;
    vec3 specularColor;
    float specularExp;
    float opacity;
};

// Uniformes
layout (std140) uniform Parameters {
    float lightPower;
    float ambientFactor;
    float diffuseFactor;
    float specularFactor;
};

// Entrées
in Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;

    // - material
    Material material;
} vecs;

// Sortie
out vec4 FragColor;

void main() {
    // vecs.normalize vectors
    vec3 n = normalize(vecs.normal);
    vec3 l = normalize(vecs.lightDirection);
    vec3 e = normalize(vecs.eyeDirection);

    // Light factor
    float distance = length(vecs.lightDirection);
    float lightFactor = lightPower / (distance * distance);

    // Prepare diffuse color
    float df = dot(n, l);
    if (df < float(0)) df = float(0);

    // Prepare specular color
    float sf = float(0);

    if (dot(n, l) > float(0)) {
        vec3 r = reflect(-l, n);
        sf = dot(e, r);

        if (sf < float(0)) {
            sf = float(0);
        } else {
            sf = pow(sf, vecs.material.specularExp);
        }
    }

    // Compute colors
    FragColor = vec4(
        // Ambient color
        (vecs.material.ambientColor * ambientFactor) +
        // Diffuse color
        (vecs.material.diffuseColor * diffuseFactor * lightFactor * df) +
        // Specular color
        (vecs.material.specularColor * specularFactor * lightFactor * sf),
        // Transparence
        vecs.material.opacity
    );
}
