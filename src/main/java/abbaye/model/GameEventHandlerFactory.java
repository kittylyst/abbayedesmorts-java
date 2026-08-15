/* Copyright (C) The Authors 2026 */
package abbaye.model;

import static abbaye.model.GameEvent.BELL_RUNG;
import static abbaye.model.Room.ROOM_ALTAR;
import static abbaye.model.Room.ROOM_TOWER;
import static abbaye.model.TileAtlas.*;
import static abbaye.model.TileAtlas.ALTAR_HATCH_COL;

public final class GameEventHandlerFactory {
  private GameEventHandlerFactory() {}

  public static Runnable bellRungEvent(GameState gs, Stage stage) {
    return () -> {
      gs.setFlag(BELL_RUNG);

      // Visual "rung" state: 301–304 → 305–308 (not cleared)
      stage.transformTilesWhere(
          ROOM_TOWER.index(),
          t -> t > TILE_BELL_MIN && t < TILE_BELL_MAX,
          t -> t + BELL_TOWER_OFFSET);

      /* Open altar hatch after bell (C game.c: flags[1], room ALTAR, x>15, tile[20][26]==7) */
      int altar = ROOM_ALTAR.index();
      stage.setTile(altar, ALTAR_HATCH_ROW, ALTAR_HATCH_COL, TILE_PLATFORM);
      stage.clearTile(altar, ALTAR_HATCH_ROW, ALTAR_HATCH_COL + 1);
      stage.clearTile(altar, ALTAR_HATCH_ROW + 1, ALTAR_HATCH_COL);
      stage.clearTile(altar, ALTAR_HATCH_ROW + 1, ALTAR_HATCH_COL + 1);
    };
  }
}
