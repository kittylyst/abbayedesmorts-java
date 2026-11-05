/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.basic.Corners;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

// ! Sub-Task 3 of tile-atlas-resource-plan.md: the hardcoded if-ladder is replaced by a lookup into
// ! /tiles/tile-atlas.json so the tile mapping data lives in a resource instead of code.
// ! No existing test fails without this change because behaviour is intentionally unchanged
// ! (abbaye.model.TestTileAtlas must keep passing). The change is still required to move the
// ! mapping data out of code; bit-for-bit parity with the old algorithm is asserted by the
// ! parity test added in Sub-Task 4.
/**
 * Maps tile-type IDs to texture atlas {@link Corners} (UV coordinates). Extracted from Stage so
 * that the mapping logic can be tested and extended independently of the level-data concerns.
 *
 * <p>The static tile-ID to pixel-rectangle table is loaded from {@code /tiles/tile-atlas.json};
 * animation and 16-bit variant offsets are applied at runtime.
 *
 * <p>Results are cached by tile-type ID; the cache is populated on first lookup.
 */
public final class TileAtlas {

  static final String ATLAS_RESOURCE = "/tiles/tile-atlas.json";

  // ── Texture atlas grid dimensions ──────────────────────────────────────────
  // Tile sheet is 125 tiles wide × 30 tiles tall (in tile units).
  public static final int TILES_PER_ROW = 125;
  public static final int TILES_PER_COL = 30;

  // ── Tile-type IDs ──────────────────────────────────────────────────────────
  // Canonical home for all tile-type integer constants. Used for both collision
  // detection (Player, Stage) and UV-coordinate lookup (TileAtlas.getCorners).

  public static final int TILE_EMPTY = 0;
  public static final int TILE_STATIC_HAZARD = 5;
  public static final int TILE_PASSABLE = 16;
  public static final int TILE_PASSABLE_VARIANT_1 = 37;
  public static final int TILE_PLATFORM = 38;
  public static final int TILE_SOLID_MAX = 100;
  public static final int TILE_TRAP_DOOR = 99;

  public static final int TILE_BEDROCK1 = 101;
  public static final int TILE_BEDROCK2 = 102;
  public static final int TILE_TOPSOIL1 = 103;
  public static final int TILE_TOPSOIL2 = 104;

  public static final int TILE_CROSS_BRIGHTNESS = 84;
  public static final int TILE_SPECIAL_COLLISION = 128;
  public static final int TILE_TORCH_LIT = 136;
  public static final int TILE_TORCH_DIM = 137;
  public static final int TILE_FLAME = 152;
  public static final int TILE_DOOR = 154;

  public static final int TILE_SPECIAL_RIGHT_MIN = 342;
  public static final int TILE_CLOSED_DOOR1 = 343;
  public static final int TILE_CLOSED_DOOR2 = 344;
  public static final int TILE_CLOSED_DOOR3 = 345;
  public static final int TILE_CLOSED_DOOR4 = 346;
  public static final int TILE_SPECIAL_LEFT = 348;
  public static final int TILE_SPECIAL_LEFT_MAX = 351; // Exclusive upper bound for range check
  public static final int TILE_SPECIAL_RIGHT_MAX = 347; // Exclusive upper bound for range check

  /** Lower bound (exclusive) for the bell sprite tile range (tiles 301–304). */
  public static final int TILE_BELL_MIN = 300;

  /** Upper bound (exclusive) for the bell sprite tile range (tiles 301–304). */
  public static final int TILE_BELL_MAX = 305;

  // ! Re-add constants accidentally removed during merge/rebase:
  // ! - BELL_TOWER_OFFSET, ALTAR_HATCH_ROW, ALTAR_HATCH_COL required by GameEventHandlerFactory and
  // TestBellInteraction
  // ! - TILE_HEART_MIN, TILE_HEART_MAX, TILE_CROSS_MIN, TILE_CROSS_MAX, TILE_WAYPOINT_MIN,
  // TILE_WAYPOINT_MAX
  // !   required by GameEventHandlerFactory, Player, and TestCollectInteractions
  // ! - ASHES_HEART_* constants for ROOM_ASHES heart coordinates
  /** Offset to add to bell sprite tile IDs when the bell has been rung. */
  public static final int BELL_TOWER_OFFSET = 4;

  public static final int ALTAR_HATCH_ROW = 20;
  public static final int ALTAR_HATCH_COL = 26;

  /** Tile range (exclusive bounds) for heart collectibles (tiles 401–404). */
  public static final int TILE_HEART_MIN = 400;

  public static final int TILE_HEART_MAX = 405;

  /**
   * In ROOM_ASHES the heart sprite at the right-side position occupies rows 7–8, cols 23–24. The
   * left-side position occupies rows 18–19, cols 8–9.
   */
  public static final int ASHES_HEART_RIGHT_ROW = 7;

  public static final int ASHES_HEART_RIGHT_COL = 23;
  public static final int ASHES_HEART_LEFT_ROW = 18;
  public static final int ASHES_HEART_LEFT_COL = 8;

  /** Tile range (exclusive bounds) for cross collectibles (tiles 409–412). */
  public static final int TILE_CROSS_MIN = 408;

  public static final int TILE_CROSS_MAX = 413;

  /** Tile range (exclusive bounds) for waypoint-cross tiles (tiles 321–326). */
  public static final int TILE_WAYPOINT_MIN = 320;

  public static final int TILE_WAYPOINT_MAX = 327;

  public static final int TILE_CUP = 650;

  /** Pixel-space rectangle into the source sprite sheet. */
  record Rect(int x, int y, int w, int h) {}

  /** Shape of {@code tile-atlas.json}. */
  record AtlasFile(
      int width,
      int height,
      int tileSize,
      @JsonProperty("default") Rect defaultRect,
      Map<Integer, Rect> tiles) {}

  private final AtlasFile atlas;

  private final Map<Integer, Corners> cache = new HashMap<>();

  /** Loads the tile table from the classpath resource; fails fast if missing or malformed. */
  public TileAtlas() {
    this(ATLAS_RESOURCE);
  }

  TileAtlas(String resourcePath) {
    try (var input = TileAtlas.class.getResourceAsStream(resourcePath)) {
      if (input == null) {
        throw new IllegalStateException("Tile atlas resource not found: " + resourcePath);
      }
      atlas = new ObjectMapper().readValue(input, AtlasFile.class);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read tile atlas resource: " + resourcePath, e);
    }
  }

  /**
   * Whether the alternate 16-bit tile row (+120 px offset) should be used. Not yet wired to a
   * runtime toggle; retained here so the field lives in the right place.
   */
  private boolean is16Bit = false;

  /**
   * Animation flip-flag for torch/flame tiles. Not yet driven by a frame counter; retained here for
   * the same reason as {@link #is16Bit}.
   */
  private boolean changeflag = false;

  /** Returns the cache (read-only view is sufficient for the tests that inspect it). */
  public Map<Integer, Corners> getCache() {
    return cache;
  }

  public void setIs16Bit(boolean is16Bit) {
    this.is16Bit = is16Bit;
  }

  public void setChangeflag(boolean changeflag) {
    this.changeflag = changeflag;
  }

  /**
   * Returns the {@link Corners} UV coordinates for the given tile-type ID. The result is cached so
   * repeated lookups for the same type are O(1).
   */
  public Corners getCorners(int tileType) {
    var cached = cache.get(tileType);
    if (cached != null) {
      return cached;
    }

    int[] counter = new int[2];

    var rect = atlas.tiles().getOrDefault(tileType, atlas.defaultRect());
    int x = rect.x();
    int y = rect.y();
    int w = rect.w();
    int h = rect.h();

    // Animation frame offsets (counters are always 0 until wired to the game loop).
    if (tileType == TILE_CROSS_BRIGHTNESS) {
      x += counter[0] / 8 * 8;
    } else if (tileType == TILE_DOOR) {
      x += (counter[0] / 8) * 16;
    } else if (tileType > 399 && tileType < 405) {
      x += 32 * (counter[0] / 15);
    } else if (tileType > 408 && tileType < 429) {
      x += 32 * (counter[1] / 23);
    }

    // 16-bit variant row. TILE_EMPTY and TILE_TRAP_DOOR are exempt (quirk of the original code).
    boolean torchOrFlame =
        tileType == TILE_FLAME || tileType == TILE_TORCH_DIM || tileType == TILE_TORCH_LIT;
    boolean exempt =
        tileType == TILE_EMPTY || tileType == TILE_TRAP_DOOR || (torchOrFlame && changeflag);
    if (is16Bit && !exempt) {
      y += 120;
    }

    float u1 = (float) x / atlas.width();
    float v1 = (float) y / atlas.height();
    float u2 = (float) (x + w) / atlas.width();
    float v2 = (float) (y + h) / atlas.height();

    var corners = new Corners(u1, 1 - v1, u2, 1 - v2);
    cache.put(tileType, corners);
    return corners;
  }
}
