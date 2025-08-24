/* Copyright (C) The Authors 2025 */
package abbaye.model;

import static abbaye.graphics.GLManager.*;

import abbaye.AbbayeMain;
import abbaye.Config;
import abbaye.basic.Corners;
import abbaye.basic.Renderable;
import abbaye.graphics.GLManager;
import abbaye.logs.GameLogger;
import java.util.HashMap;
import java.util.Map;

public class StatusDisplay implements Renderable {

  private final Player player;
  private final Stage stage;

  private GameLogger logger = Config.config().getLogger();
  private GLManager manager;

  public record Glyph(String name, int width, int height, Corners corners) {}

  private float GLYPH_HEIGHT = 0.041666668f;

  /** Glyphs for each char (digits) for hearts and crosses */
  private final Map<Character, Glyph> digitGlyphs = new HashMap<>();

  private final Map<Integer, Glyph> roomTitles = new HashMap<>();

  /** Height of the font. */
  private int fontHeight;

  private StatusDisplay(Player player, Stage stage) {
    this.player = player;
    this.stage = stage;
  }

  public static StatusDisplay of(Player p, Stage s) {
    return new StatusDisplay(p, s);
  }

  @Override
  public void init() {
    if (AbbayeMain.isGlEnabled()) {
      manager = GLManager.get("game");
    }
    for (byte i = 0; i < 10; i += 1) {
      char c = (char) ('0' + i);
      float u = i * 0.07f;
      var corners = new Corners(u, 0, u + 0.05f, GLYPH_HEIGHT);
      digitGlyphs.put(c, new Glyph("" + c, 64, 64, corners));
    }

    // Top row has a slightly weird layout
    for (int i = 1; i <= 3; i += 1) {
      float v = GLYPH_HEIGHT * (25 - i);
      var corners = new Corners(0, v - GLYPH_HEIGHT, 1.0f, v);
      roomTitles.put(i, new Glyph(Room.values()[i].name(), 64, 64, corners));
    }
    for (int i = 5; i <= 25; i += 1) {
      float v = GLYPH_HEIGHT * (26 - i);
      var corners = new Corners(0, v - GLYPH_HEIGHT, 1.0f, v);
      roomTitles.put(i, new Glyph(Room.values()[i].name(), 64, 64, corners));
    }
  }

  void renderStaticTitle(int tileId, int x, int y) {
    var corners = stage.getCorners(tileId);

    var tileDisplaySize = Stage.getTileSize();
    var tr = createTranslationMatrix(x * tileDisplaySize, (22 + y) * tileDisplaySize, 0);
    float[] scale = createScaleMatrix(tileDisplaySize, tileDisplaySize, 1);
    manager.renderTile(corners, multiplyMatrices(scale, tr));
  }

  @Override
  public boolean render() {
    if (!Config.config().getGLActive()) {
      return false;
    }

    // Render the heart and crosses from the primary tilemap
    manager.bindTexture("tiles");
    renderStaticTitle(401, 0, 0);
    renderStaticTitle(402, 1, 0);
    renderStaticTitle(403, 0, 1);
    renderStaticTitle(404, 1, 1);

    //      /* Crosses */
    //      if ((tileType > 408) && (tileType < 429)) {
    //          srctiles.x = 96 + ((tileType - 401) * 8) + (32 * (counter[1] / 23));
    //          srctiles.y = 24;
    //          srctiles.w = 8;
    //          srctiles.h = 8;
    //      }
    renderStaticTitle(409, 20, 0);
    renderStaticTitle(410, 21, 0);
    renderStaticTitle(411, 20, 1);
    renderStaticTitle(412, 21, 1);

    // Swap to the font texture
    manager.bindTexture("fonts");

    renderText("" + player.getLives(), 2, 0);
    renderText("" + player.getCrosses(), 22, 0);

    // Render room title
    Glyph roomLegend = roomTitles.get(stage.getRoom());
    var tileDisplaySize = Stage.getTileSize();
    var m1 = createTranslationMatrix(5 * tileDisplaySize, 25 * tileDisplaySize, 0);
    float[] scale = createScaleMatrix(10 * tileDisplaySize, -tileDisplaySize, 1);
    manager.renderTile(roomLegend.corners(), multiplyMatrices(scale, m1));

    return false;
  }

  /**
   * Draw text at the specified position and color.
   *
   * @param text Text to draw
   * @param x X render coordinate of the top-left text position
   * @param y Y render coordinate of the top-left text position
   */
  public void renderText(CharSequence text, final float x, final float y) {
    int textHeight = getHeight(text);
    int fontHeight = getFontHeight();

    // drawX and drawY represent where we're going to render
    float drawX = x;
    float drawY = y;
    if (textHeight > fontHeight) {
      drawY += textHeight - fontHeight;
    }

    var tileDisplaySize = Stage.getTileSize();
    for (int i = 0; i < text.length(); i++) {
      drawX = (x + i) * tileDisplaySize;
      char ch = text.charAt(i);
      if (ch == '\n') {
        /* Line feed, set x and y to draw at the next line */
        drawY -= fontHeight;
        //        drawX = x;
        continue;
      }
      if (ch == '\r') {
        /* Carriage return, just skip it */
        continue;
      }
      Glyph g = digitGlyphs.get(ch);

      var m1 = createTranslationMatrix(drawX, 24 * tileDisplaySize, 0);
      float[] scale = createScaleMatrix(tileDisplaySize, -tileDisplaySize, 1);
      manager.renderTile(g.corners(), multiplyMatrices(scale, m1));
      //      drawX += g.width();
    }
  }

  /**
   * Gets the width of the specified text.
   *
   * @param text The text
   * @return Width of text
   */
  public int getWidth(CharSequence text) {
    int width = 0;
    int lineWidth = 0;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\n') {
        /* Line end, set width to maximum from line width and stored
         * width */
        width = Math.max(width, lineWidth);
        lineWidth = 0;
        continue;
      }
      if (c == '\r') {
        /* Carriage return, just skip it */
        continue;
      }
      Glyph g = digitGlyphs.get(c);
      lineWidth += g.width();
    }
    width = Math.max(width, lineWidth);
    return width;
  }

  /**
   * Gets the height of the specified text.
   *
   * @param text The text
   * @return Height of text
   */
  public int getHeight(CharSequence text) {
    int height = 0;
    int lineHeight = 0;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\n') {
        /* Line end, add line height to stored height */
        height += lineHeight;
        lineHeight = 0;
        continue;
      }
      if (c == '\r') {
        /* Carriage return, just skip it */
        continue;
      }
      Glyph g = digitGlyphs.get(c);
      lineHeight = Math.max(lineHeight, g.height());
    }
    height += lineHeight;
    return height;
  }

  public int getFontHeight() {
    return fontHeight;
  }
}
