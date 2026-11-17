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

const float STRIPE_FREQ  = 1.5;
const float STRIPE_K     = 600.0;
const float STRIPE_THICK = 0.10;
const vec3  STRIPE_COLOR = vec3(0.35, 0.85, 1.0);
const float STRIPE_GLOW  = 0.8;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    float bands  = fract(viewPos.y * STRIPE_FREQ - GameTime * STRIPE_K);
    float stripe = smoothstep(0.0, STRIPE_THICK, bands) * smoothstep(STRIPE_THICK * 2.0, STRIPE_THICK, bands);
    color.rgb += STRIPE_COLOR * stripe * STRIPE_GLOW;
    color.a   += stripe * 0.2;

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}