/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

/**
 * Raw parsed data for a single enemy slot from {@code enemies.txt}.
 *
 * <p>Field names and semantics match the C {@code struct enem} arrays; see {@code
 * docs/ENEMIES_FORMAT.md} for the full reference. All pixel values are in C native resolution (8
 * px/tile). Multiply by {@link Player#PIXELS_PER_TILE} to obtain Java world coordinates.
 *
 * <p>Slots with {@code type == EnemyType.UNKNOWN} (code 0) are absent and should be skipped.
 */
record EnemyData(
    EnemyType type,
    int x,
    int y,
    int direction,
    int tileX,
    int tileY,
    int animation,
    int limitLeft,
    int limitRight,
    int speed,
    int fire,
    int adjustX1,
    int adjustX2,
    int adjustY1,
    int adjustY2) {

  /** Number of integer fields per slot in the data file. */
  static final int FIELD_COUNT = 15;

  /**
   * Parses one slot from a flat array of {@link #FIELD_COUNT} integers as read from the file.
   *
   * @param fields array of exactly {@value #FIELD_COUNT} integers
   * @return parsed {@code EnemyData}
   * @throws IllegalArgumentException if {@code fields.length != FIELD_COUNT}
   */
  static EnemyData fromFields(int[] fields) {
    if (fields.length != FIELD_COUNT) {
      throw new IllegalArgumentException(
          "Expected " + FIELD_COUNT + " fields, got " + fields.length);
    }
    return new EnemyData(
        EnemyType.fromCode(fields[0]),
        fields[1],
        fields[2],
        fields[3],
        fields[4],
        fields[5],
        fields[6],
        fields[7],
        fields[8],
        fields[9],
        fields[10],
        fields[11],
        fields[12],
        fields[13],
        fields[14]);
  }

  /**
   * Returns {@code true} if this slot should produce a live enemy. Delegates to {@link
   * EnemyType#isLive()}: empty slots (code 0) and spawn markers ({@link EnemyType#CRUSADER_SPAWN})
   * both return {@code false}.
   */
  boolean isPresent() {
    return type.isLive();
  }
}
