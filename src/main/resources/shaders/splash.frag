#version 330 core

out vec4 FragColor;

in vec2 TexCoord;

uniform sampler2D splashTexture;
uniform float alpha;

void main() {
    vec4 texColor = texture(splashTexture, TexCoord);
    FragColor = vec4(texColor.rgb, texColor.a * alpha);
}

