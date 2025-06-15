#version 330 core

out vec4 FragColor;

in vec2 TexCoord;

uniform sampler2D logoTexture;
uniform float alpha;

void main() {
    vec4 texColor = texture(logoTexture, TexCoord);
    FragColor = vec4(texColor.rgb, texColor.a * alpha);
}

