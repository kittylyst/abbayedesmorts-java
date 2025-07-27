/* Copyright (C) The Authors 2025 */
package abbaye.model;

public enum Room {
  ROOM_NULL(0, "NULL"),
  ROOM_PRAYER(1, "A prayer of hope"),
  ROOM_TOWER(2, "Tower of the Bell"),
  ROOM_WINE(3, "Wine supplies"),
  ROOM_THEEND(4, "THE END"),
  ROOM_ESCAPE(5, "Escape!!!"), // 5 - (0,1) entry point
  ROOM_CLOSE(6, "Death is close"),
  ROOM_CHURCH(7, "Abandoned church"),
  ROOM_ALTAR(8, "The Altar"),
  ROOM_HANGMAN(9, "Hangman Tree"),
  ROOM_BEAST(10, "Pestilent Beast"), // 10 - (0,2)
  ROOM_CAVE(11, "Cave of illusions"),
  ROOM_RUINS(12, "Plagued ruins"),
  ROOM_CATACOMBS(13, "Catacombs"),
  ROOM_GARDEN(14, "Hidden garden"),
  ROOM_TUNNELS(15, "Gloomy tunnels"), // 15
  ROOM_LAKE(16, "Lake of despair"),
  ROOM_WHEEL(17, "The wheel of faith"),
  ROOM_BANQUET(18, "Banquet of Death"),
  ROOM_RIVER(19, "Underground river"),
  ROOM_GATE(20, "Unexpected gate"), // 20
  ROOM_EVIL(21, "Evil church"),
  ROOM_SOULS(22, "Tortured souls"),
  ROOM_ASHES(23, "Ashes to ashes"),
  ROOM_SATAN(24, "Satan !!!"),
  ROOM_INVALID(25, "Invalid Room");

  private final int index;
  private final String name;

  Room(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public int index() {
    return index;
  }
}
