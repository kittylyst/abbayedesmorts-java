/* Copyright (C) The Authors 2025 */
package abbaye.basic;

public record Vector2(float x, float y) {
  public float magnitude() {
    return (float) Math.sqrt(x * x + y * y);
  }

  public Vector2 normalize() {
    float m = magnitude();

    return new Vector2(x / m, y / m);
  }

  public Vector2 scale(float s) {
    return new Vector2(x * s, y * s);
  }
}
