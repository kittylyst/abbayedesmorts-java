/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Facing.RIGHT;

import abbaye.basic.Actor;
import abbaye.basic.Vector2;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Enemy implements Actor {

  private final EnemyType type;

  // Physicality
  private Vector2 pos = new Vector2(0, 0);
  private Vector2 v = new Vector2(0, 0);
  private Facing direction = RIGHT;

  public static class EnemySerializer extends JsonSerializer<Enemy> {
    @Override
    public void serialize(
        Enemy enemy, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
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
  public Facing getDirection() {
    return direction;
  }

  private Enemy(EnemyType type, Vector2 pos) {
    this.type = type;
    this.pos = pos;
  }

  public static Enemy of(EnemyType type) {
    return new Enemy(type, new Vector2(0, 0));
  }

  /**
   * Constructs an {@code Enemy} from parsed file data. Position is converted from C native pixels
   * to Java world pixels by multiplying by {@link Player#PIXELS_PER_TILE}.
   */
  static Enemy of(EnemyData data) {
    float worldX = data.x() * Player.PIXELS_PER_TILE;
    float worldY = data.y() * Player.PIXELS_PER_TILE;
    var enemy = new Enemy(data.type(), new Vector2(worldX, worldY));
    enemy.direction = data.direction() == 1 ? Facing.LEFT : Facing.RIGHT;
    return enemy;
  }

  @Override
  public Vector2 getSize() {
    return type.getSize();
  }

  @Override
  public boolean render() {
    return false;
  }
}
