/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import abbaye.basic.Corners;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps tile-type IDs to texture atlas {@link Corners} (UV coordinates). Extracted from Stage so
 * that the mapping logic can be tested and extended independently of the level-data concerns.
 *
 * <p>Results are cached by tile-type ID; the cache is populated on first lookup.
 */
public final class TileAtlas {

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

  public static final int TILE_CUP = 650;

  /** Offset to add to bell sprite tile IDs when the bell has been rung. */
  public static final int BELL_TOWER_OFFSET = 4;

  public static final int ALTAR_HATCH_ROW = 20;
  public static final int ALTAR_HATCH_COL = 26;
  private static final int ALTAR_HATCH_CLOSED_TILE = 7;

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

  /** Pixel-space rectangle into the source sprite sheet (8-px grid). */
  private static final class SDLRect {
    int x, y, w, h;

    SDLRect(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }

  private final Map<Integer, Corners> cache = new HashMap<>();

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

    var srctiles = new SDLRect(0, 0, 8, 8);
    if (tileType == TILE_EMPTY) {
      srctiles = new SDLRect(992, 0, 8, 8);
    } else if (tileType != TILE_TRAP_DOOR) {
      if (tileType < 200) {
        srctiles.w = 8;
        srctiles.h = 8;
        if (tileType < 101) {
          srctiles.y = 0;
          if (tileType == TILE_CROSS_BRIGHTNESS) {
            srctiles.x = (tileType - 1) * 8 + (counter[0] / 8 * 8);
          } else {
            srctiles.x = (tileType - 1) * 8;
          }
        } else {
          if (tileType == TILE_DOOR) {
            srctiles.x = 600 + ((counter[0] / 8) * 16);
            srctiles.y = 0;
            srctiles.w = 16;
            srctiles.h = 24;
          } else {
            srctiles.y = 8;
            srctiles.x = (tileType - 101) * 8;
          }
        }
      }
      if (tileType > 199 && tileType < 300) {
        srctiles.x = (tileType - 201) * 48;
        srctiles.y = 16;
        srctiles.w = 48;
        srctiles.h = 48;
      }
      if (tileType > 299 && tileType < 399) {
        srctiles.x = 96 + ((tileType - 301) * 8);
        srctiles.y = 16;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      /* Hearts */
      if (tileType > 399 && tileType < 405) {
        srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[0] / 15));
        srctiles.y = 24;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      /* Crosses */
      if (tileType > 408 && tileType < 429) {
        srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[1] / 23));
        srctiles.y = 24;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      if (tileType > 499 && tileType < 599) {
        srctiles.x = 96 + ((tileType - 501) * 8);
        srctiles.y = 32;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      if (tileType > 599 && tileType < 650) {
        srctiles.x = 96 + ((tileType - 601) * 8);
        srctiles.y = 56;
        srctiles.w = 8;
        srctiles.h = 8;
      }
      if (tileType == TILE_CUP) {
        srctiles.x = 584;
        srctiles.y = 87;
        srctiles.w = 16;
        srctiles.h = 16;
      }
      if (tileType == TILE_FLAME || tileType == TILE_TORCH_DIM || tileType == TILE_TORCH_LIT) {
        if (!changeflag) {
          srctiles.y = srctiles.y + (is16Bit ? 120 : 0);
        }
      } else {
        srctiles.y = srctiles.y + (is16Bit ? 120 : 0);
      }
    }

    float u1 = (float) srctiles.x / (8 * TILES_PER_ROW);
    float v1 = (float) srctiles.y / (8 * TILES_PER_COL);
    float u2 = (float) (srctiles.x + srctiles.w) / (8 * TILES_PER_ROW);
    float v2 = (float) (srctiles.y + srctiles.h) / (8 * TILES_PER_COL);

    var corners = new Corners(u1, 1 - v1, u2, 1 - v2);
    cache.put(tileType, corners);
    return corners;
  }
}
