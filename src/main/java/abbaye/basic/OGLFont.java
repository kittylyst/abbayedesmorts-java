/* Copyright (C) The Authors 2025 */
package abbaye.basic;

import abbaye.AbbayeMain;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;

public final class OGLFont {
  // build colours for font with alpha transparency
  private static final Color OPAQUE_WHITE = new Color(0xFFFFFFFF, true);
  private static final Color TRANSPARENT_BLACK = new Color(0x00000000, true);

  private int base;
  private int texture;

  public void print(String msg, Vector2 pos) {
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glEnable(GL11.GL_BLEND);

    GL11.glPushMatrix();
    GL11.glTranslatef(pos.x(), pos.y(), 0);
    if (msg != null) {
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
      for (int i = 0; i < msg.length(); i++) {
        GL11.glCallList(base + msg.charAt(i));
      }
    }
    GL11.glPopMatrix();

    GL11.glDisable(GL11.GL_BLEND);
  }

  public void buildFont(String fontName, int fontSize) { // Build Our Bitmap Font
    if (!AbbayeMain.isGlEnabled()) {
      return;
    }

    Font font; // Font object

    /* Note that I have set the font to Courier New.  This font is not guraunteed to be on all
     * systems.  However it is very common so it is likely to be there.  You can replace this name
     * with any named font on your system or use the Java default names that are guraunteed to be there.
     * Also note that this will work well with monospace fonts, but does not look as good with
     * proportional fonts.
     */
    // String fontName = "Courier New";                // Name of the font to use
    BufferedImage fontImage; // image for creating the bitmap
    int bitmapSize = 512; // set the size for the bitmap texture
    boolean sizeFound = false;
    boolean directionSet = false;
    int delta = 0;
    // int fontSize = 24;

    /* To find out how much space a Font takes, you need to use a the FontMetrics class.
     * To get the FontMetrics, you need to get it from a Graphics context.  A Graphics context is
     * only available from a displayable surface, ie any class that subclasses Component or any Image.
     * First the font is set on a Graphics object.  Then get the FontMetrics and find out the width
     * and height of the widest character (W).  Then take the largest of the 2 values and find the
     * maximum size font that will fit in the size allocated.
     */
    while (!sizeFound) {
      font = new Font(fontName, Font.PLAIN, fontSize); // Font Name
      // use BufferedImage.TYPE_4BYTE_ABGR to allow alpha blending
      fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D g = (Graphics2D) fontImage.getGraphics();
      g.setFont(font);
      FontMetrics fm = g.getFontMetrics();
      int width = fm.stringWidth("W");
      int height = fm.getHeight();
      int lineWidth = (width > height) ? width * 16 : height * 16;
      if (!directionSet) {
        if (lineWidth > bitmapSize) {
          delta = -2;
        } else {
          delta = 2;
        }
        directionSet = true;
      }
      if (delta > 0) {
        if (lineWidth < bitmapSize) {
          fontSize += delta;
        } else {
          sizeFound = true;
          fontSize -= delta;
        }
      } else if (delta < 0) {
        if (lineWidth > bitmapSize) {
          fontSize += delta;
        } else {
          sizeFound = true;
          fontSize -= delta;
        }
      }
    }

    /* Now that a font size has been determined, create the final image, set the font and draw the
     * standard/extended ASCII character set for that font.
     */
    font = new Font(fontName, Font.BOLD, fontSize); // Font Name
    // use BufferedImage.TYPE_4BYTE_ABGR to allow alpha blending
    fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g = (Graphics2D) fontImage.getGraphics();
    g.setFont(font);
    g.setColor(OPAQUE_WHITE);
    g.setBackground(TRANSPARENT_BLACK);
    FontMetrics fm = g.getFontMetrics();
    for (int i = 0; i < 256; i++) {
      int x = i % 16;
      int y = i / 16;
      char ch[] = {(char) i};
      String temp = new String(ch);
      g.drawString(temp, (x * 32) + 1, (y * 32) + fm.getAscent());
    }

    /* The following code is taken directly for the LWJGL example code.
     * It takes a Java Image and converts it into an OpenGL texture.
     * This is a very powerful feature as you can use this to generate textures on the fly out
     * of anything.
     */
    //      Flip Image
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -fontImage.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    fontImage = op.filter(fontImage, null);

    // Put Image In Memory
    ByteBuffer scratch =
        ByteBuffer.allocateDirect(4 * fontImage.getWidth() * fontImage.getHeight());

    byte data[] =
        (byte[])
            fontImage
                .getRaster()
                .getDataElements(0, 0, fontImage.getWidth(), fontImage.getHeight(), null);
    scratch.clear();
    scratch.put(data);
    scratch.rewind();

    // Create A IntBuffer For Image Address In Memory
    IntBuffer buf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
    GL11.glGenTextures(buf); // Create Texture In OpenGL

    GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(0));
    // Typical Texture Generation Using Data From The Image

    // Linear Filtering
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    // Linear Filtering
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    // Generate The Texture
    GL11.glTexImage2D(
        GL11.GL_TEXTURE_2D,
        0,
        GL11.GL_RGBA,
        fontImage.getWidth(),
        fontImage.getHeight(),
        0,
        GL11.GL_RGBA,
        GL11.GL_UNSIGNED_BYTE,
        scratch);

    texture = buf.get(0); // Return Image Address In Memory

    base = GL11.glGenLists(256); // Storage For 256 Characters

    /* Generate the display lists.  One for each character in the standard/extended ASCII chart.
     */
    float textureDelta = 1.0f / 16.0f;
    for (int i = 0; i < 256; i++) {
      float u = ((float) (i % 16)) / 16.0f;
      float v = 1.f - (((float) (i / 16)) / 16.0f);
      GL11.glNewList(base + i, GL11.GL_COMPILE);
      // GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
      GL11.glBegin(GL11.GL_QUADS);
      GL11.glTexCoord2f(u, (v - textureDelta));
      GL11.glVertex2f(0.0f, fm.getHeight());
      GL11.glTexCoord2f(u, v);
      GL11.glVertex2f(0.0f, 0.0f);
      GL11.glTexCoord2f((u + textureDelta), v);
      GL11.glVertex2f(fm.charWidth((char) i), 0);
      GL11.glTexCoord2f((u + textureDelta), (v - textureDelta));
      GL11.glVertex2f(fm.charWidth((char) i), fm.getHeight());
      GL11.glEnd();
      GL11.glTranslatef(fm.charWidth((char) i) - 3, 0, 0);
      GL11.glEndList();
    }
  }
}
