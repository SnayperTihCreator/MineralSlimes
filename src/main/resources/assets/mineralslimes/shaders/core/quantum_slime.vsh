#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float GameTime;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec3 viewPos;
out vec3 viewNormal;

const float BAND_H    = 1.5;
const float SHIFT_AMP = 0.9;

void main() {
    float ticks = GameTime * 24000.0;
    float frame = mod(floor(ticks / 3.0), 1024.0);

    float bandId = floor(Position.y / BAND_H);
    float r1 = fract(sin(bandId * 17.13 + frame * 91.17) * 43758.5453);
    float r2 = fract(sin(bandId * 29.71 + frame * 57.31) * 43758.5453);
    float dir   = mod(bandId, 2.0) * 2.0 - 1.0;
    float gate  = step(0.35, r1);
    float shift = gate * (r2 - 0.5) * 2.0 * SHIFT_AMP * dir;

    vec3 pos = Position;
    pos.x += shift;
    pos.z += (r1 - 0.5) * 0.2;

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;

    viewPos = Position;
    viewNormal = Normal;
}