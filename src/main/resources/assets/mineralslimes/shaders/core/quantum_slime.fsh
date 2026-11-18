#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewPos;
in vec3 viewNormal;

out vec4 fragColor;

const float BAND_H = 1.5;
const vec3 CYAN = vec3(0.35, 0.85, 1.0);
const vec3 DEEP_BLUE = vec3(0.07, 0.20, 0.45);

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    float ticks  = GameTime * 24000.0;
    float frame  = mod(floor(ticks / 3.0), 1024.0);
    float bandId = floor(viewPos.y / BAND_H);

    float tone = fract(sin(bandId * 12.7 + frame * 3.17) * 43758.5453);
    vec3 layer = mix(CYAN, DEEP_BLUE, step(0.5, tone));

    float lum = 0.35 + 0.8 * fract(sin(bandId * 5.71 + frame * 13.7) * 43758.5453);
    float hole = step(0.85, fract(sin(bandId * 41.3 + frame * 27.1) * 43758.5453));

    color.rgb = mix(color.rgb, layer, 0.55);
    color.rgb *= lum;
    color.rgb += layer * 0.2;

    color.a *= 0.8;
    color.a *= mix(0.45, 1.0, lum);

    color *= 1.0 - hole;
    if (color.a < 0.05) {
        discard;
    }

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}