/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static abbaye.model.TileAtlas.TILES_PER_COL;
import static abbaye.model.TileAtlas.TILES_PER_ROW;
import static abbaye.model.TileAtlas.TILE_CROSS_BRIGHTNESS;
import static abbaye.model.TileAtlas.TILE_CUP;
import static abbaye.model.TileAtlas.TILE_DOOR;
import static abbaye.model.TileAtlas.TILE_EMPTY;
import static abbaye.model.TileAtlas.TILE_FLAME;
import static abbaye.model.TileAtlas.TILE_TORCH_DIM;
import static abbaye.model.TileAtlas.TILE_TORCH_LIT;
import static abbaye.model.TileAtlas.TILE_TRAP_DOOR;

import abbaye.basic.Corners;

// ! Legacy reference implementation of the tile-to-UV mapping algorithm from TileAtlas.
// ! Used as an oracle to generate tile-atlas.json and to verify that the resource-based
// ! TileAtlas implementation maintains bit-for-bit parity across all tile IDs and flag
// ! combinations.
// ! Tested by: abbaye.model.TestLegacyTileAtlasReference
public final class LegacyTileAtlasReference {

  private LegacyTileAtlasReference() {}

  /** Pixel-space rectangle in the sprite sheet. */
  public record PixelRect(int x, int y, int w, int h) {}

  /**
   * Computes the pixel rectangle for the given tile type, 16-bit flag, and changeflag using the
   * legacy if-ladder logic, with animation counters fixed at 0.
   */
  public static PixelRect computePixelRect(int tileType, boolean is16Bit, boolean changeflag) {
    int[] counter = new int[2];

    int x = 0;
    int y = 0;
    int w = 8;
    int h = 8;

    if (tileType == TILE_EMPTY) {
      x = 992;
      y = 0;
      w = 8;
      h = 8;
    } else if (tileType != TILE_TRAP_DOOR) {
      if (tileType < 200) {
        w = 8;
        h = 8;
        if (tileType < 101) {
          y = 0;
          if (tileType == TILE_CROSS_BRIGHTNESS) {
            x = (tileType - 1) * 8 + (counter[0] / 8 * 8);
          } else {
            x = (tileType - 1) * 8;
          }
        } else {
          if (tileType == TILE_DOOR) {
            x = 600 + ((counter[0] / 8) * 16);
            y = 0;
            w = 16;
            h = 24;
          } else {
            y = 8;
            x = (tileType - 101) * 8;
          }
        }
      }
      if (tileType > 199 && tileType < 300) {
        x = (tileType - 201) * 48;
        y = 16;
        w = 48;
        h = 48;
      }
      if (tileType > 299 && tileType < 399) {
        x = 96 + ((tileType - 301) * 8);
        y = 16;
        w = 8;
        h = 8;
      }
      /* Hearts */
      if (tileType > 399 && tileType < 405) {
        x = 96 + ((tileType - 401) * 8) + (32 * (counter[0] / 15));
        y = 24;
        w = 8;
        h = 8;
      }
      /* Crosses */
      if (tileType > 408 && tileType < 429) {
        x = 96 + ((tileType - 401) * 8) + (32 * (counter[1] / 23));
        y = 24;
        w = 8;
        h = 8;
      }
      if (tileType > 499 && tileType < 599) {
        x = 96 + ((tileType - 501) * 8);
        y = 32;
        w = 8;
        h = 8;
      }
      if (tileType > 599 && tileType < 650) {
        x = 96 + ((tileType - 601) * 8);
        y = 56;
        w = 8;
        h = 8;
      }
      if (tileType == TILE_CUP) {
        x = 584;
        y = 87;
        w = 16;
        h = 16;
      }
      if (tileType == TILE_FLAME || tileType == TILE_TORCH_DIM || tileType == TILE_TORCH_LIT) {
        if (!changeflag) {
          y = y + (is16Bit ? 120 : 0);
        }
      } else {
        y = y + (is16Bit ? 120 : 0);
      }
    }

    return new PixelRect(x, y, w, h);
  }

  /** Converts a {@link PixelRect} to {@link Corners} UV coordinates using the atlas dimensions. */
  public static Corners toCorners(PixelRect rect) {
    float u1 = (float) rect.x() / (8 * TILES_PER_ROW);
    float v1 = (float) rect.y() / (8 * TILES_PER_COL);
    float u2 = (float) (rect.x() + rect.w()) / (8 * TILES_PER_ROW);
    float v2 = (float) (rect.y() + rect.h()) / (8 * TILES_PER_COL);
    return new Corners(u1, 1 - v1, u2, 1 - v2);
  }

  /** Computes the UV corners for a given tile type using the legacy reference logic. */
  public static Corners computeCorners(int tileType, boolean is16Bit, boolean changeflag) {
    return toCorners(computePixelRect(tileType, is16Bit, changeflag));
  }
}
