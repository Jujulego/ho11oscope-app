#version 310 es
layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

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
} gs_in[];

out Vectors {
    // - world space
    vec3 position;

    // - camera space
    vec3 eyeDirection;
    vec3 lightDirection;
    vec3 normal;
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

    gl_Position = explode(gl_in[0].gl_Position, normal);
    gs_out = gs_in[0];
    EmitVertex();

    gl_Position = explode(gl_in[1].gl_Position, normal);
    gs_out = gs_in[1];
    EmitVertex();

    gl_Position = explode(gl_in[2].gl_Position, normal);
    gs_out = gs_in[2];
    EmitVertex();

    EndPrimitive();
}
