/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.Facing.LEFT;
import static abbaye.model.Facing.RIGHT;
import static abbaye.model.TileAtlas.TILES_PER_COL;
import static abbaye.model.TileAtlas.TILES_PER_ROW;

import abbaye.AbbayeMain;
import abbaye.Config;
import abbaye.basic.Actor;
import abbaye.basic.BoundingBox2;
import abbaye.basic.Corners;
import abbaye.basic.Vector2;
import abbaye.graphics.GLManager;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public final class Enemy implements Actor {

  private final EnemyType type;

  // Physicality — in Java world pixels (C native px × PIXELS_PER_TILE)
  private Vector2 pos;
  private final Vector2 v = new Vector2(0, 0);
  private Facing direction;

  // Patrol bounds and speed — stored in Java world pixels
  private final float limitLeft;
  private final float limitRight;

  /**
   * Sprite atlas source coordinates in C native pixels (as stored in {@code enemies.txt} fields 4
   * and 5). Used directly to compute UV coordinates for rendering.
   */
  private final int tileX;

  private final int tileY;

  // GL rendering
  private GLManager manager;

  /**
   * Patrol speed in Java world pixels per frame. Derived from the C {@code speed} field: {@code
   * speed * 0.10 * PIXELS_PER_TILE}.
   */
  private final float patrolSpeed;

  /**
   * Collision-box adjustments in Java world pixels. Applied as offsets from {@link #pos} when
   * computing the hit box used for player contact detection.
   */
  private final float adjustX1;

  private final float adjustX2;
  private final float adjustY1;
  private final float adjustY2;

  // ── Serializer (no-op stub for Jackson game-state dump) ─────────────────────

  public static class EnemySerializer extends JsonSerializer<Enemy> {
    @Override
    public void serialize(
        Enemy enemy, JsonGenerator generator, SerializerProvider serializerProvider)
        throws IOException {}
  }

  // ── Accessors ────────────────────────────────────────────────────────────────

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

  EnemyType getType() {
    return type;
  }

  @Override
  public Vector2 getSize() {
    return type.getSize();
  }

  /**
   * Returns the collision bounding box for this enemy in Java world pixels, built from the stored
   * adjust offsets (matching the C {@code contact()} function).
   */
  BoundingBox2 hitBox() {
    float left = pos.x() + adjustX1;
    float right = pos.x() + adjustX2;
    float top = pos.y() + adjustY1;
    float bottom = pos.y() + adjustY2;
    float cx = (left + right) / 2f;
    float cy = (top + bottom) / 2f;
    return new BoundingBox2(new Vector2(cx, cy), new Vector2(right - left, bottom - top));
  }

  // ── Constructors ─────────────────────────────────────────────────────────────

  private Enemy(
      EnemyType type,
      Vector2 pos,
      Facing direction,
      float limitLeft,
      float limitRight,
      float patrolSpeed,
      float adjustX1,
      float adjustX2,
      float adjustY1,
      float adjustY2,
      int tileX,
      int tileY) {
    this.type = type;
    this.pos = pos;
    this.direction = direction;
    this.limitLeft = limitLeft;
    this.limitRight = limitRight;
    this.patrolSpeed = patrolSpeed;
    this.adjustX1 = adjustX1;
    this.adjustX2 = adjustX2;
    this.adjustY1 = adjustY1;
    this.adjustY2 = adjustY2;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  /** Creates a bare enemy with default (zero) patrol bounds — used in tests. */
  public static Enemy of(EnemyType type) {
    return new Enemy(type, new Vector2(0, 0), RIGHT, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  }

  /**
   * Constructs an {@code Enemy} from parsed file data. All pixel values are converted from C native
   * resolution to Java world pixels by multiplying by {@link Player#PIXELS_PER_TILE}.
   */
  static Enemy of(EnemyData data) {
    float scale = Player.PIXELS_PER_TILE;
    float worldX = data.x() * scale;
    float worldY = data.y() * scale;
    Facing dir = data.direction() == 1 ? LEFT : RIGHT;
    float ll = data.limitLeft() * scale;
    float lr = data.limitRight() * scale;
    // C speed: pixels/frame = speed * 0.10 in native res; Java world = × scale
    float speed = data.speed() * 0.10f * scale;
    float ax1 = data.adjustX1() * scale;
    float ax2 = data.adjustX2() * scale;
    float ay1 = data.adjustY1() * scale;
    float ay2 = data.adjustY2() * scale;
    var out =
        new Enemy(
            data.type(),
            new Vector2(worldX, worldY),
            dir,
            ll,
            lr,
            speed,
            ax1,
            ax2,
            ay1,
            ay2,
            data.tileX(),
            data.tileY());
    out.init();
    return out;
  }

  // ── Game-loop methods ────────────────────────────────────────────────────────

  /**
   * Updates this enemy's position for one game tick.
   *
   * <p>Only types 1–9 (standard patrol enemies) are moved; all others are stationary stubs pending
   * their type-specific implementations in Phase 6. Movement mirrors the C {@code movenemies()}
   * patrol loop: advance in the current direction by {@link #patrolSpeed} per tick, reverse at
   * patrol boundaries.
   */
  @Override
  public boolean update() {
    if (type.code < 1 || type.code > 9) {
      return true; // non-patrol types: no movement yet
    }
    switch (direction) {
      case RIGHT -> {
        if (pos.x() + 1 < limitRight) {
          pos = new Vector2(pos.x() + patrolSpeed, pos.y());
        } else {
          direction = LEFT;
        }
      }
      case LEFT -> {
        if (pos.x() - 1 > limitLeft) {
          pos = new Vector2(pos.x() - patrolSpeed, pos.y());
        } else {
          direction = RIGHT;
        }
      }
    }
    return true;
  }

  @Override
  public void init() {
    if (AbbayeMain.isGlEnabled()) {
      manager = GLManager.get("game");
    }
  }

  @Override
  public boolean render() {
    if (!Config.config().getGLActive()) {
      return false;
    }

    // Atlas dimensions in C-native pixels
    int atlasW = 8 * TILES_PER_ROW;
    int atlasH = 8 * TILES_PER_COL;

    // Sprite dimensions in C-native pixels
    int spriteW = (int) type.getSize().x();
    int spriteH = (int) type.getSize().y();

    // UV coordinates for the sprite in the atlas (Y flipped for OpenGL)
    float u1 = (float) tileX / atlasW;
    float u2 = (float) (tileX + spriteW) / atlasW;
    float v1 = 1f - (float) tileY / atlasH;
    float v2 = 1f - (float) (tileY + spriteH) / atlasH;

    // Display dimensions: each C-native pixel maps to (tileSize / PIXELS_PER_TILE) display pixels
    float scale = Stage.getTileSize() / Player.PIXELS_PER_TILE;
    float dispW = spriteW * scale;
    float dispH = spriteH * scale;

    // Flip U horizontally for LEFT-facing sprites
    Corners tileCoords;
    if (direction == LEFT) {
      tileCoords = new Corners(u2, v1, u1, v2);
    } else {
      tileCoords = new Corners(u1, v1, u2, v2);
    }

    float[] translateM = GLManager.createTranslationMatrix(pos.x(), pos.y(), 0);
    float[] scaleM = GLManager.createScaleMatrix(dispW, dispH, 1);
    float[] model = GLManager.multiplyMatrices(scaleM, translateM);
    manager.renderTile(tileCoords, model);

    return true;
  }
}
