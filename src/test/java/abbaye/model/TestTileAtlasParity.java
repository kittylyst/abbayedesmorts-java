/* Copyright (C) The Authors 2025-2026 */
package abbaye.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import abbaye.model.TileAtlasJsonGenerator.TileAtlasFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

// ! Sub-Task 4 of tile-atlas-resource-plan.md. Proves the JSON-backed TileAtlas matches the legacy
// ! if-ladder (LegacyTileAtlasReference) exactly.
// ! Fails without the Sub-Task 3 change if: tile-atlas.json is missing an entry, the 16-bit
// ! exemptions for TILE_EMPTY / TILE_TRAP_DOOR / torch+flame are lost, the animation remap is
// ! dropped, or the UV arithmetic changes by even 1 ulp (parityAcrossAllIdsAndFlagCombinations);
// ! TILES_PER_ROW/TILES_PER_COL drift from the JSON metadata (constantsMatchJsonMetadata);
// ! a missing resource is silently tolerated (missingResourceFailsFast).
public class TestTileAtlasParity {

  @Test
  public void parityAcrossAllIdsAndFlagCombinations() {
    for (boolean is16Bit : new boolean[] {false, true}) {
      for (boolean changeflag : new boolean[] {false, true}) {
        // Fresh instance per combination: the cache is not invalidated when flags change.
        var atlas = new TileAtlas();
        atlas.setIs16Bit(is16Bit);
        atlas.setChangeflag(changeflag);
        // Starts at 0: the legacy ladder extrapolated a nonsense negative x for negative IDs,
        // which the map parser (non-negative 3-digit fields) can never produce. Upper bound
        // extends past the table to cover the default-rect fallback.
        for (int id = 0; id <= 700; id++) {
          assertEquals(
              LegacyTileAtlasReference.computeCorners(id, is16Bit, changeflag),
              atlas.getCorners(id),
              "id=" + id + " is16Bit=" + is16Bit + " changeflag=" + changeflag);
        }
      }
    }
  }

  @Test
  public void constantsMatchJsonMetadata() throws IOException {
    try (var in = TileAtlas.class.getResourceAsStream(TileAtlas.ATLAS_RESOURCE)) {
      var file = new ObjectMapper().readValue(in, TileAtlasFile.class);
      assertEquals(file.tileSize() * TileAtlas.TILES_PER_ROW, file.width());
      assertEquals(file.tileSize() * TileAtlas.TILES_PER_COL, file.height());
    }
  }

  @Test
  public void missingResourceFailsFast() {
    var ex = assertThrows(IllegalStateException.class, () -> new TileAtlas("/tiles/missing.json"));
    assertTrue(ex.getMessage().contains("/tiles/missing.json"));
  }
}
