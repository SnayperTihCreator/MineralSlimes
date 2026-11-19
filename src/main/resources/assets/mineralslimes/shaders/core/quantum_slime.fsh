#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 vPos;

out vec4 fragColor;

const float ZONES = 16.0;
const vec3  CYAN = vec3(0.45, 1.00, 1.00);
const vec3  DEEP_BLUE = vec3(0.05, 0.12, 0.30);
const float GLOW = 0.5;
const float HOLO_A  = 0.55;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;

    float zoneId = floor(vPos.y * ZONES);
    float parity = mod(zoneId, 2.0);
    color.rgb = mix(CYAN, DEEP_BLUE, parity);

    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color.rgb *= mix(lightMapColor.rgb, vec3(1.0), GLOW);

    color.a *= HOLO_A;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}