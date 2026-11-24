#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float AnimTime;
uniform float AnimSpeed;
uniform float ModifySat;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const float PX = 0.125;

float hash11(float n) {
    return fract(sin(n) * 43758.5453123);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}


float river(vec2 p, float t, float seed, float P, out float hue) {
    float s1 = hash11(seed);
    float s2 = hash11(seed + 11.7);
    float s3 = hash11(seed + 27.3);

    float cx = sin(p.y * (0.8 + s1 * 0.8) + t * (0.35 + s2 * 0.4) + seed * 7.0) * P * (0.45 + s3 * 0.25)
    + sin(p.y * (2.6 + s2 * 0.6) - t * (0.6 + s1 * 0.5) + seed * 13.0) * P * 0.20;

    float coord = (p.x + cx) / P + t * (0.02 + s3 * 0.04);
    float d = abs(fract(coord) - 0.5) * P;

    float w = P * (0.10 + 0.07 * sin(p.y * (6.0 + s1 * 5.0) - t * (1.2 + s2) + seed * 3.0));

    hue = fract(coord * 0.5 + t * (0.06 + s1 * 0.05) + seed * 0.618);
    return 1.0 - smoothstep(0.0, w, d);
}

float zones(vec2 p, float t, float baseHue, out vec3 zcol) {
    const float SX = 4.5;
    const float SY = 4.0;
    float m = 0.0;
    zcol = vec3(0.0);

    for (int i = 0; i < 3; ++i) {
        float fi = float(i) + 1.0;
        float s1 = hash11(fi * 5.13);
        float s2 = hash11(fi * 9.71);
        float s3 = hash11(fi * 13.37);
        float dirx = step(0.5, s3) * 2.0 - 1.0;

        float cx = fract(s1 + dirx * t * (0.05 + 0.05 * s2)) * SX;
        float cy = fract(s2 - t * (0.06 + 0.05 * s1)) * SY;

        float dx = abs(mod(p.x, SX) - cx);
        dx = min(dx, SX - dx);
        float dy = abs(mod(p.y, SY) - cy);
        dy = min(dy, SY - dy);
        float d = sqrt(dx * dx + dy * dy);

        float r = (0.9 + 0.7 * s1) * (0.85 + 0.25 * sin(t * 0.7 + fi * 2.3) + 0.2 * sin(p.x * 1.3 + t * 0.5));
        float k = 1.0 - smoothstep(0.0, r, d);

        float flow = 0.55 + 0.45 * sin(p.x * 1.2 + sin(p.y * 1.1 - t * 0.7) * 1.5 - t * 0.9);
        k *= flow;

        float hue = fract(baseHue + fi * 0.37 + t * (0.05 + 0.03 * s2) + d * 0.06);
        zcol += hsv2rgb(vec3(hue, 0.65*ModifySat, 1.0)) * k;
        m += k;
    }
    return m;
}

void main() {
    float t = AnimTime * 1.25 * AnimSpeed;
    vec2 p = (floor(texCoord0 / PX) + 0.5) * PX;
    float baseHue = fract(p.x * 0.02 + p.y * 0.012 + t * 0.03);

    float ripple = 0.5 + 0.5 * sin(p.x * 1.6 + sin(p.y * 0.9 + t * 0.6) * 1.2 + t * 0.8);
    vec3 membrane = hsv2rgb(vec3(fract(baseHue + 0.15), 0.55*ModifySat, 1.0)) * (0.05 + 0.10 * ripple);

    vec3 zcol;
    float zm = zones(p, t, baseHue, zcol);
    zm = min(zm, 1.25);

    vec3 rcol = vec3(0.0);
    float glow = 0.0;
    for (int i = 0; i < 3; ++i) {
        float fi = float(i);
        float hue;
        float r = river(p, t * (1.0 + fi * 0.3), fi * 9.17 + 3.1, 1.7 + fi * 1.4, hue);
        rcol += hsv2rgb(vec3(hue, (0.85 - fi * 0.2)*ModifySat, 1.0)) * r * (1.0 - fi * 0.22);
        glow += r;
    }

    float sparkle = pow(max(0.0, sin(p.x * 21.0 + t * 2.7) * sin(p.y * 4.0 - t * 1.9)), 12.0);

    vec3 color = membrane + zcol * 0.55 + rcol * 0.7
    + vec3(1.0) * sparkle * min(glow + zm * 0.5, 1.0) * 0.7;

    float alpha = clamp(0.07 + zm * 0.22 + glow * 0.5 + ripple * 0.06, 0.0, 1.0) * vertexColor.a;
    vec4 c = vec4(color, alpha) * ColorModulator;

    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd);
    fragColor = vec4(c.rgb * fade, c.a);
}
