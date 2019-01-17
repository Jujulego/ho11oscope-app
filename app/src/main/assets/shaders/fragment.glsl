#version 320 es
#extension GL_EXT_shader_io_blocks : enable

precision mediump float;

// Structures
struct Material {
    vec3 ambientColor;
    vec3 diffuseColor;
    vec3 specularColor;

    float specularExp;
    float opacity;

    int hasTexture;
    sampler2D texture;
};

// Uniformes
layout (std140) uniform Parameters {
    float lightPower;
    float ambientFactor;
    float diffuseFactor;
    float specularFactor;
};

uniform Material material;
uniform sampler2D matTexture;

// Entrées
in Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;

    // - textures
    vec2 uv;
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

    if (material.specularExp != float(0) && dot(n, l) > float(0)) {
        vec3 r = reflect(-l, n);
        sf = dot(e, r);

        if (sf < float(0)) {
            sf = float(0);
        } else {
            sf = pow(sf, material.specularExp);
        }
    }

    // Apply diffuse texture on diffuse color base
    vec3 diffuseColor = material.diffuseColor;

    if (material.hasTexture != 0) {
        diffuseColor = texture(material.texture, vecs.uv).xyz;
    }

    // Compute color
    FragColor = vec4(
        // Ambient color
        (material.ambientColor * ambientFactor) +
        // Diffuse color
        (diffuseColor * diffuseFactor * lightFactor * df) +
        // Specular color
        (material.specularColor * specularFactor * lightFactor * sf),
        // Transparence
        material.opacity
    );
}
