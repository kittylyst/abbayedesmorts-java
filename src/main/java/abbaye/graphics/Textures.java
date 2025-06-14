/* Copyright (C) The Authors 2004-2025 */
package abbaye.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

/** Textures utility class */
public final class Textures {
  private Textures() {}

  private static Map<String, Integer> textures = new HashMap<>();

  public static int loadTexture(String path, boolean isResource) {
    int texture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texture);

    // Set texture wrapping/filtering options
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    // Load and generate texture
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      // Flip image vertically for OpenGL
      stbi_set_flip_vertically_on_load(true);

      ByteBuffer image = null;
      if (isResource) {
        // Load resource as ByteBuffer
        var imageBuffer = loadResourceAsBuffer(path);
        if (imageBuffer == null) {
          System.err.println("Failed to load resource: " + path);
          return -1;
        }
        image = stbi_load_from_memory(imageBuffer, width, height, channels, 4);
      } else {
        image = stbi_load(path, width, height, channels, 4);
      }

      if (image != null) {
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width.get(0),
            height.get(0),
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            image);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(image);
      } else {
        System.err.println("Failed to load texture: " + path);
        // Create a default white texture
        ByteBuffer defaultTexture = BufferUtils.createByteBuffer(4);
        defaultTexture.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255);
        defaultTexture.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, defaultTexture);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return texture;
  }

  //  public static int loadTexture(String path, boolean isResource) {
  //    ByteBuffer imageBuffer = null;
  //
  //    try (MemoryStack stack = stackPush()) {
  //      IntBuffer w = stack.mallocInt(1);
  //      IntBuffer h = stack.mallocInt(1);
  //      IntBuffer channels = stack.mallocInt(1);
  //
  //      // Load image data from ByteBuffer
  //      stbi_set_flip_vertically_on_load(true);
  //
  //      ByteBuffer decodedImage = null;
  //      if (isResource) {
  //        // Load resource as ByteBuffer
  //        imageBuffer = loadResourceAsBuffer(path);
  //        if (imageBuffer == null) {
  //          System.err.println("Failed to load resource: " + path);
  //          return -1;
  //        }
  //        decodedImage = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
  //      } else {
  //        decodedImage = stbi_load(path, w, h, channels, 4);
  //      }
  //
  //      if (decodedImage == null) {
  //        System.err.println("Failed to decode image: " + path);
  //        System.err.println("STB Error: " + stbi_failure_reason());
  //        return -1;
  //      }
  //
  //      int textureId = glGenTextures();
  //      glBindTexture(GL_TEXTURE_2D, textureId);
  //
  //      // Set texture parameters
  //      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
  //      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
  //      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  //      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  //
  //      // Upload texture data
  //      glTexImage2D(
  //          GL_TEXTURE_2D,
  //          0,
  //          GL_RGBA,
  //          w.get(0),
  //          h.get(0),
  //          0,
  //          GL_RGBA,
  //          GL_UNSIGNED_BYTE,
  //          decodedImage);
  //
  //      // Free image memory
  //      stbi_image_free(decodedImage);
  //
  //      System.out.println(
  //          "Loaded texture from resource: " + path + " (" + w.get(0) + "x" + h.get(0) + ")");
  //      return textureId;
  //
  //    } catch (Exception e) {
  //      System.err.println("Exception loading texture resource " + path + ": " + e.getMessage());
  //      e.printStackTrace();
  //      return -1;
  //    } finally {
  //      // Free the resource buffer if it was allocated
  //      if (imageBuffer != null) {
  //        memFree(imageBuffer);
  //      }
  //    }
  //  }

  /**
   * Returns an
   *
   * @param resourcePath
   * @return
   * @throws IOException
   */
  public static ByteBuffer loadResourceAsBuffer(String resourcePath) throws IOException {
    try (var inputStream = Textures.class.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + resourcePath);
      }

      var data = inputStream.readAllBytes();

      // Create a ByteBuffer and copy the data
      ByteBuffer byteBuffer = memAlloc(data.length);
      byteBuffer.put(data);
      byteBuffer.flip();

      return byteBuffer;

    } catch (IOException e) {
      throw new IOException("Failed to read resource: " + resourcePath, e);
    }
  }
}
