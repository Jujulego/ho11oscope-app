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

struct PointLight {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;

    vec3 position;
    float constant;
    float linear;
    float quadratic;
};

// Uniformes
layout (std140) uniform Matrices {
    mat4 mvpMatrix;
    mat4 modelMatrix;
    mat4 lightMatrix;
};

uniform Material material;
uniform PointLight light;

// Entrées
in Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 normal;

    // - textures
    vec2 uv;
} vecs;

// Sortie
out vec4 FragColor;

// Prototypes
vec3 getAmbientColor();
vec3 getDiffuseColor();
vec3 getSpecularColor();

vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir);

// Fonctions
void main() {
    // Prepare
    vec3 normal = normalize(vecs.normal);
    vec3 eyeDir = normalize(vecs.eyeDirection);

    vec3 result = calcPointLight(light, normal, vecs.position, eyeDir);

    FragColor = vec4(result, material.opacity);
}

vec3 getAmbientColor() {
    if (material.hasTexture == 0) {
        return material.ambientColor;
    } else {
        return texture(material.texture, vecs.uv).xyz;
    }
}
vec3 getDiffuseColor() {
    if (material.hasTexture == 0) {
        return material.diffuseColor;
    } else {
        return texture(material.texture, vecs.uv).xyz;
    }
}
vec3 getSpecularColor() {
    return material.specularColor;
}

vec3 calcPointLight(PointLight light, vec3 normal, vec3 pos, vec3 viewDir) {
    vec3 lightPos = (lightMatrix * vec4(light.position, 1)).xyz;
    vec3 lightDir = normalize(lightPos - pos);

    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.specularExp);

    // attenuation
    float distance    = length(lightPos - pos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    // combine results
    vec3 ambient  = light.ambient  * vec3(getAmbientColor());
    vec3 diffuse  = light.diffuse  * diff * vec3(getDiffuseColor());
    vec3 specular = light.specular * spec * vec3(getSpecularColor());

    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;

    return (ambient + diffuse + specular);
}