#version 320 es
#extension GL_EXT_shader_io_blocks : enable

layout (triangles) in;
layout (line_strip, max_vertices = 3) out;

// Structures
uniform float magnitude;

// Entrée - Sorties
in Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;

    // - textures
    vec2 uv;
} gs_in[];

out Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;

    // - textures
    vec2 uv;
} gs_out;

// Fonctions
vec3 getNormal() {
    vec3 a = vec3(gl_in[0].gl_Position) - vec3(gl_in[1].gl_Position);
    vec3 b = vec3(gl_in[2].gl_Position) - vec3(gl_in[1].gl_Position);
    return normalize(cross(a, b));
}

vec4 explode(vec4 position, vec3 normal) {
    if (magnitude < 0.01) {
        return position;
    }

    vec3 direction = normal * magnitude;
    return position + vec4(direction, 0.0);
}

void main() {
    vec3 normal = getNormal();
    vec4 pts[3];

    for (int i = 0; i < 3; ++i) {
        gl_Position = explode(gl_in[i].gl_Position, normal);
        gs_out = gs_in[(i+1) % 3];
        EmitVertex();
    }
    EndPrimitive();
}
