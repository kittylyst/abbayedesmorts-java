/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static org.lwjgl.glfw.GLFW.GLFW_HAT_RIGHT;

import abbaye.basic.Actor;
import abbaye.basic.BoundingBox2;
import abbaye.basic.Vector2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Enemy implements Actor {

  // Physicality
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private int direction = GLFW_HAT_RIGHT;

  public static class EnemySerializer extends JsonSerializer<Enemy> {
    @Override
    public void serialize(
        Enemy enemy, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  @Override
  public BoundingBox2 getBB() {
    return null;
  }

  @Override
  public Vector2 getPos() {
    return pos;
  }

  @Override
  public Vector2 getV() {
    return v;
  }

  @Override
  public int getDirection() {
    return direction;
  }

  @Override
  public Vector2 getSize() {
    return null;
  }

  @Override
  public void init() {
    Actor.super.init();
  }

  @Override
  public boolean render() {
    return false;
  }

  @Override
  public boolean update() {
    return Actor.super.update();
  }
}
