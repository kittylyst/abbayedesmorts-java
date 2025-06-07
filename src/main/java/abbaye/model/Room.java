/* Copyright (C) The Authors 2025 */
package abbaye.model;

public enum Room {
  ROOM_NULL("NULL"),
  ROOM_PRAYER("A prayer of hope"),
  ROOM_TOWER("Tower of the Bell"),
  ROOM_WINE("Wine supplies"),
  ROOM_THEEND("THE END"),
  ROOM_ESCAPE("Escape!!!"), // 5 - (0,1) entry point
  ROOM_CLOSE("Death is close"),
  ROOM_CHURCH("Abandoned church"),
  ROOM_ALTAR("The Altar"),
  ROOM_HANGMAN("Hangman Tree"),
  ROOM_BEAST("Pestilent Beast"), // 10 - (0,2)
  ROOM_CAVE("Cave of illusions"),
  ROOM_RUINS("Plagued ruins"),
  ROOM_CATACOMBS("Catacombs"),
  ROOM_GARDEN("Hidden garden"),
  ROOM_TUNNELS("Gloomy tunnels"), // 15
  ROOM_LAKE("Lake of despair"),
  ROOM_WHEEL("The wheel of faith"),
  ROOM_BANQUET("Banquet of Death"),
  ROOM_RIVER("Underground river"),
  ROOM_GATE("Unexpected gate"), // 20
  ROOM_EVIL("Evil church"),
  ROOM_SOULS("Tortured souls"),
  ROOM_ASHES("Ashes to ashes"),
  ROOM_SATAN("Satan !!!"),
  ROOM_INVALID("Invalid Room");

  private final String name;

  Room(String name) {
    this.name = name;
  }
}
