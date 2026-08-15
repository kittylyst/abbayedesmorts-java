/* Copyright (C) The Authors 2026 */
package abbaye.model;

import static abbaye.model.GameEvent.*;
import static abbaye.model.Room.ROOM_ALTAR;
import static abbaye.model.Room.ROOM_TOWER;
import static abbaye.model.TileAtlas.*;

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

  /**
   * Returns a handler for {@link GameEvent#HEART_COLLECTED}: sweep-clears all heart tiles (401–404)
   * from the current room and awards the player one life (capped at 9).
   */
  public static Runnable heartCollectedEvent(Stage stage, Player player) {
    return () -> {
      stage.clearTilesWhere(stage.getRoom(), t -> t > TILE_HEART_MIN && t < TILE_HEART_MAX);
      player.addLife();
    };
  }

  /**
   * Returns a handler for {@link GameEvent#CROSS_COLLECTED}: sweep-clears all cross tiles (409–412)
   * from the current room and increments the player's cross count.
   */
  public static Runnable crossCollectedEvent(Stage stage, Player player) {
    return () -> {
      stage.clearTilesWhere(stage.getRoom(), t -> t > TILE_CROSS_MIN && t < TILE_CROSS_MAX);
      player.addCross();
    };
  }

  /**
   * Returns a handler for {@link GameEvent#WAYPOINT_REACHED}: sweep-clears all waypoint-cross tiles
   * (321–326) from the current room.
   */
  public static Runnable waypointReachedEvent(Stage stage) {
    return () ->
        stage.clearTilesWhere(stage.getRoom(), t -> t > TILE_WAYPOINT_MIN && t < TILE_WAYPOINT_MAX);
  }
}
