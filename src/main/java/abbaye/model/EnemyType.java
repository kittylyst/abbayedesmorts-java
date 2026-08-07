/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.basic.Vector2;

/**
 * The type of an enemy, carrying its sprite dimensions for collision detection.
 *
 * <p>Type codes and sprite sizes are derived from the C GPL source ({@code enemies.c}, {@code
 * drawenemies}). Only codes that appear in {@code enemies.txt} are represented as named values; see
 * {@code docs/ENEMIES_FORMAT.md} for the full reference.
 *
 * <p>Sprite sizes are in C native pixels (8 px/tile). The Java display scale is 8×, so sizes must
 * be multiplied by {@code Stage.getTileSize() / Player.PIXELS_PER_TILE} when used for rendering.
 */
public enum EnemyType {

  // ── Standard horizontal/vertical patrol enemies (types 1–6) ─────────────────
  /** Basic ground patrol, 16×16. Flips sprite on turn. */
  WALKER(1, new Vector2(16, 16)),

  /** Ground patrol, 16×16. Does NOT flip sprite on direction change. */
  WALKER_NO_FLIP(2, new Vector2(16, 16)),

  /** Tall ground patrol, 16×24. Flips sprite on turn. */
  TALL_WALKER(3, new Vector2(16, 24)),

  /** Vertical floater, 16×16. {@code tilex} shifts 32 px on direction change. */
  FLOATER_V(4, new Vector2(16, 16)),

  /** Tall vertical floater, 16×24. {@code tilex} shifts 16 px on direction change. */
  TALL_FLOATER_V(5, new Vector2(16, 24)),

  /** Wide horizontal patrol, 24×16. {@code tilex} shifts 48 px on turn. */
  WIDE_WALKER(6, new Vector2(24, 16)),

  // ── Special/boss enemies ────────────────────────────────────────────────────
  /**
   * Stationary shooter, 16×24. Fires a projectile at Jean when its {@code speed} counter reaches
   * 50. Slots 1–6 mirror the state of slot 0 for synchronised firing.
   */
  SHOOTER(11, new Vector2(16, 24)),

  /**
   * Water drop, 16×16. Falls from {@code limleft} Y to {@code limright} Y, then resets. {@code
   * speed} is repurposed as a frame counter; {@code tilex} is derived from it.
   */
  WATER_DROP(12, new Vector2(16, 16)),

  /**
   * Fireball / rising projectile, 16×16. Bounces vertically between {@code limleft} and {@code
   * limright} Y. Draws a splash sprite at {@code limright} while surfacing.
   */
  FIREBALL(13, new Vector2(16, 16)),

  /**
   * Fish / submerged variant of fireball, 16×16. Same vertical bounce logic as {@link #FIREBALL},
   * but no splash rendering.
   */
  FISH(14, new Vector2(16, 16)),

  /**
   * Aiming shooter, 16×24. Flips sprite to face Jean before firing; otherwise identical to {@link
   * #SHOOTER}.
   */
  AIMING_SHOOTER(15, new Vector2(16, 24)),

  /**
   * Crusader, 16×24. Marches rightward; can jump over obstacles in specific rooms. Slots with
   * {@link #CRUSADER_SPAWN} (type 17) in the data file are initialised to this type at room load.
   */
  CRUSADER(16, new Vector2(16, 24)),

  /**
   * Crusader spawn marker (type 17 in file). Converted to {@link #CRUSADER} at runtime during
   * {@code searchenemies}; never rendered directly.
   */
  CRUSADER_SPAWN(17, new Vector2(16, 24)),

  /**
   * Satan boss, 32×24. Vertical patrol with triple-shot projectile attack. Only appears in {@code
   * ROOM_SATAN}.
   */
  SATAN(18, new Vector2(32, 24)),

  /** Smoke effect, 32×48. Stationary; animated via {@code speed} counter. */
  SMOKE(88, new Vector2(32, 48)),

  /** Fallback for any unrecognised type code encountered during parsing. */
  UNKNOWN(0, new Vector2(16, 16));

  // ── Fields ──────────────────────────────────────────────────────────────────

  /** The integer type code as stored in {@code enemies.txt}. */
  public final int code;

  private final Vector2 size;

  EnemyType(int code, Vector2 size) {
    this.code = code;
    this.size = size;
  }

  public Vector2 getSize() {
    return size;
  }

  /**
   * Returns {@code true} if this type should be instantiated as a live {@link Enemy} at room load.
   *
   * <p>{@link #CRUSADER_SPAWN} is a data-file marker that the C engine converts to {@link
   * #CRUSADER} during {@code searchenemies}; it is never rendered or used as a hazard directly.
   * {@link #UNKNOWN} (code 0) denotes an empty slot.
   */
  public boolean isLive() {
    return this != UNKNOWN && this != CRUSADER_SPAWN;
  }

  /**
   * Returns {@code true} if this type patrols vertically (Y axis). Types 4 ({@link #FLOATER_V}) and
   * 5 ({@link #TALL_FLOATER_V}) use {@code limitLeft}/{@code limitRight} as Y bounds.
   */
  public boolean isVerticalPatrol() {
    return this == FLOATER_V || this == TALL_FLOATER_V;
  }

  /**
   * Returns the {@code EnemyType} for the given file code, or {@link #UNKNOWN} if the code is not
   * recognised.
   */
  public static EnemyType fromCode(int code) {
    for (var t : values()) {
      if (t.code == code) return t;
    }
    return UNKNOWN;
  }
}
