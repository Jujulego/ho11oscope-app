#version 320 es
#extension GL_EXT_shader_io_blocks : enable

layout (std140) uniform Matrices {
    mat4 mvpMatrix;
    mat4 modelMatrix;
};

layout (std140) uniform Stables {
    mat4 viewMatrix;
    mat4 projMatrix;

    vec3 lightPosition; // in world space
};

in vec3 aPos;
in vec3 aNormal;

out VS_OUT {
    vec3 normal;
} vs_out;

void main() {
    mat3 normalMatrix = mat3(transpose(inverse(viewMatrix * modelMatrix)));

    vs_out.normal = vec3(projMatrix * vec4(normalMatrix * aNormal, 0.0));
    gl_Position = mvpMatrix * vec4(aPos, 1.0);
}