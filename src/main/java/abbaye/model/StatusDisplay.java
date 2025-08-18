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

  private GameLogger logger = Config.config().getLogger();
  private GLManager manager;
  private int texture;

  public record Glyph(int width, int height, int x, int y, float advance) {}

  /** Contains the glyphs for each char. */
  private final Map<Character, Glyph> glyphs = new HashMap<>();

  /** Height of the font. */
  private int fontHeight;

  private StatusDisplay(Player player) {
    this.player = player;
  }

  public static StatusDisplay of(Player p) {
    return new StatusDisplay(p);
  }

  @Override
  public void init() {
    if (AbbayeMain.isGlEnabled()) {
      manager = GLManager.get("game");
      texture = GLManager.loadTexture("/fonts.png", true, true);
    }
    //    for (var i = 0; i < 10; i += 1) {
    //      glyphs.put(i, new Glyph());
    //    }
  }

  @Override
  public boolean render() {
    manager.bindTexture(texture);

    var scoreText = "" + player.getLives(); // "Lives: " +
    logger.info(scoreText);
    if (!Config.config().getGLActive()) {
      return false;
    }
    // FIXME
    //        renderText(scoreText, 0, 0);
    var tileDisplaySize = Stage.getTileSize();
    for (var i = 0; i < 10; i += 1) {
      float u = i * 0.05f;
      var tileCoords = new Corners(u, 0.8f, u + 0.05f, 0.85f);
      var m1 = createTranslationMatrix(i * tileDisplaySize, 22 * tileDisplaySize, 0);
      float[] scale = createScaleMatrix(tileDisplaySize, tileDisplaySize, 1);
      manager.renderTile(tileCoords, multiplyMatrices(scale, m1));
    }

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

    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '\n') {
        /* Line feed, set x and y to draw at the next line */
        drawY -= fontHeight;
        drawX = x;
        continue;
      }
      if (ch == '\r') {
        /* Carriage return, just skip it */
        continue;
      }
      Glyph g = glyphs.get(ch);

      // glyphCoords represents where in the tile texture to pick out the player tile that we'll
      // render
      //      var glyphCoords = g.getCorners(44, 11);
      //      manager.renderTile(glyphCoords, renderMatrix(drawX, drawY, Stage.getTileSize()));

      drawX += g.width();
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
      Glyph g = glyphs.get(c);
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
      Glyph g = glyphs.get(c);
      lineHeight = Math.max(lineHeight, g.height());
    }
    height += lineHeight;
    return height;
  }

  public int getFontHeight() {
    return fontHeight;
  }

  //  /**
  //   * Draw text at the specified position.
  //   *
  //   * @param text Text to draw
  //   * @param x X coordinate of the text position
  //   * @param y Y coordinate of the text position
  //   */
  //  public void drawText(CharSequence text, float x, float y) {
  //    drawText(text, x, y, Color.WHITE);
  //  }
}
