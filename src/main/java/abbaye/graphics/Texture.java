/* Copyright (C) The Authors 2025 */
package abbaye.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

public final class Texture {
  private final int id;
  private final int width;
  private final int height;

  /** Creates a texture. */
  private Texture(int width, int height) {
    this.width = width;
    this.height = height;
    id = glGenTextures();
  }

  /** Binds the texture. */
  public void bind() {
    glBindTexture(GL_TEXTURE_2D, id);
  }

  /**
   * Sets a parameter of the texture.
   *
   * @param name Name of the parameter
   * @param value Value to set
   */
  public void setParameter(int name, int value) {
    glTexParameteri(GL_TEXTURE_2D, name, value);
  }

  /**
   * Uploads image data with specified width and height.
   *
   * @param width Width of the image
   * @param height Height of the image
   * @param data Pixel data of the image
   */
  public void uploadData(int width, int height, ByteBuffer data) {
    uploadData(GL_RGBA8, width, height, GL_RGBA, data);
  }

  /**
   * Uploads image data with specified internal format, width, height and image format.
   *
   * @param internalFormat Internal format of the image data
   * @param width Width of the image
   * @param height Height of the image
   * @param format Format of the image data
   * @param data Pixel data of the image
   */
  public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
    glTexImage2D(
        GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
  }

  /** Delete the texture. */
  public void delete() {
    glDeleteTextures(id);
  }

  /**
   * Gets the texture width.
   *
   * @return Texture width
   */
  public int getWidth() {
    return width;
  }

  public int getId() {
    return id;
  }

  /**
   * Gets the texture height.
   *
   * @return Texture height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Creates a texture with specified width, height and data.
   *
   * @param width Width of the texture
   * @param height Height of the texture
   * @param data Picture Data in RGBA format
   * @return Texture from the specified data
   */
  public static Texture of(int width, int height, ByteBuffer data) {
    Texture texture = new Texture(width, height);
    texture.bind();

    texture.setParameter(GL_TEXTURE_WRAP_S, GL_REPEAT);
    texture.setParameter(GL_TEXTURE_WRAP_T, GL_REPEAT);
    texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data);

    return texture;
  }

  /**
   * Load texture from file or resource
   *
   * @param path File or resource path of the texture
   * @param isResource
   * @param shouldFlip
   * @return Texture from specified file or resource
   */
  public static Texture of(String path, boolean isResource, boolean shouldFlip) {

    // Load texture as a buffer
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      // Flip image vertically for OpenGL
      if (shouldFlip) {
        stbi_set_flip_vertically_on_load(true);
      }

      ByteBuffer image = null;
      if (isResource) {
        // Load resource as ByteBuffer or throw
        var imageBuffer = loadResourceAsBuffer(path);
        image = stbi_load_from_memory(imageBuffer, width, height, channels, 4);
      } else {
        image = stbi_load(path, width, height, channels, 4);
      }

      return of(width.get(0), height.get(0), image);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns an
   *
   * @param resourcePath
   * @return
   * @throws IOException
   */
  public static ByteBuffer loadResourceAsBuffer(String resourcePath) throws IOException {
    try (var inputStream = GLManager.class.getResourceAsStream(resourcePath)) {
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
