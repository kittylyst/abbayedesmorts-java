/* Copyright (C) The Authors 2004-2025 */
package abbaye.basic;

import abbaye.AbbayeMain;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.lwjgl.opengl.GL11;

/** Textures utility class */
public final class Textures {
  private Textures() {}

  private static Map<String, Integer> textures = new HashMap<>();

  public static synchronized int loadTextureMirrored(String pathStr) {
    return loadTexture(pathStr, true, true);
  }

  public static synchronized int loadTexture(String pathStr) {
    return loadTexture(pathStr, true, false);
  }

  public static synchronized int loadTexture(
      String pathStr, boolean isResource, boolean withMirror) {
    if (!AbbayeMain.isGlEnabled()) {
      return 0;
    }
    // check if we already loaded this texture
    if (textures.containsKey(pathStr)) {
      // Return the existing texture's ID if it is already loaded
      return textures.get(pathStr);
    }
    BufferedImage bufferedImage;

    if (isResource) {
      bufferedImage = getBufferedImageFromResource(pathStr);
    } else {
      bufferedImage = getBufferedImageFromFile(pathStr);
    }
    BufferedImage image =
        withMirror ? flipImageWithMirror(bufferedImage) : flipImage(bufferedImage);

    // Put Image In Memory
    ByteBuffer scratch = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());

    byte data[] =
        (byte[]) image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
    scratch.clear();
    scratch.put(data);
    scratch.rewind();

    // Create A IntBuffer For Image Address In Memory
    IntBuffer buf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
    GL11.glGenTextures(buf); // Create Texture ID In OpenGL
    int textureID = buf.get(0);

    // Bind the generated texture ID for use
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

    // Set texture parameters (linear filtering for magnification and minification)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

    // Generate The Texture in OpenGL
    GL11.glTexImage2D(
        GL11.GL_TEXTURE_2D,
        0,
        GL11.GL_RGB,
        image.getWidth(),
        image.getHeight(),
        0,
        GL11.GL_RGB,
        GL11.GL_UNSIGNED_BYTE,
        scratch);

    // add the texture to the list
    textures.put(pathStr, textureID);

    // Return Texture ID
    return textureID;
  }

  /**
   * @param pathStr
   * @return
   */
  public static BufferedImage getBufferedImageFromResource(String pathStr) {
    Image image = (new ImageIcon(Textures.class.getResource(pathStr))).getImage();
    return imageToBufferedImage(image);
  }

  /**
   * @param fileName
   * @return
   */
  public static BufferedImage getBufferedImageFromFile(String fileName) {
    Image image = (new ImageIcon(fileName)).getImage();
    return imageToBufferedImage(image);
  }

  /**
   * Convert the image to a BufferedImage and draw it
   *
   * @param image
   * @return
   */
  public static BufferedImage imageToBufferedImage(Image image) {
    BufferedImage tex =
        new BufferedImage(
            image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = (Graphics2D) tex.getGraphics();
    g.drawImage(image, null, null);
    g.dispose();

    return tex;
  }

  /** Flip the image vertically to match OpenGL's texture coordinate system */
  public static BufferedImage flipImage(BufferedImage tex) {
    AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
    transform.translate(0, -tex.getHeight());
    AffineTransformOp op =
        new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(tex, null);
  }

  /**
   * Flip the image vertically to match OpenGL's texture coordinate system, and also mirror it in
   * the x-axis
   */
  public static BufferedImage flipImageWithMirror(BufferedImage tex) {
    AffineTransform transform = AffineTransform.getScaleInstance(-1, -1);
    transform.translate(-tex.getWidth(), -tex.getHeight(null));
    AffineTransformOp op =
        new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    return op.filter(tex, null);
  }
}
