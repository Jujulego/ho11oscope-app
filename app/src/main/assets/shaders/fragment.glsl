// Uniformes
// - valeurs
uniform float uLightPower;

// Communes au shaders
// - world space
varying vec3 position;

// - camera space
varying vec3 eyeDirection;
varying vec3 lightDirection;
varying vec3 normal;

// - couleur
varying vec3 ambientColor;
varying vec3 diffuseColor;
varying vec3 specularColor;
varying float specularExp;
varying float opacity;

void main() {
    // Normalize vectors
    vec3 n = normalize(normal);
    vec3 l = normalize(lightDirection);
    vec3 e = normalize(eyeDirection);

    // Light factor
    float distance = length(lightDirection);
    float lightFactor = uLightPower / (distance * distance);

    // Prepare diffuse color
    float diffuseFactor = dot(n, l);
    if (diffuseFactor < float(0)) diffuseFactor = float(0);

    // Prepare specular color
    float specularFactor = float(0);

    if (dot(n, l) > float(0)) {
        vec3 r = reflect(-l, n);
        specularFactor = dot(e, r);

        if (specularFactor < float(0)) {
            specularFactor = float(0);
        } else {
            specularFactor = pow(specularFactor, specularExp);
        }
    }

    // Compute colors
    gl_FragColor = vec4(
        // Ambient color
        (ambientColor * 0.1) +
        // Diffuse color
        (diffuseColor * lightFactor * diffuseFactor) +
        // Specular color
        (specularColor * lightFactor * specularFactor),
        // Transparence
        opacity
    );
}
