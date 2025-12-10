/* Copyright (C) The Authors 2025 */
package abbaye.basic;

import abbaye.model.Stage;

public record Vector2(float x, float y) {
  public static final Vector2 ORIGIN = new Vector2(0, 0);

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

  public int tileX() {
    float resize = Stage.getTileSize();
    return (int) (x() / resize);
  }

  public int tileY() {
    float resize = Stage.getTileSize();
    return (int) (y() / resize);
  }
}
