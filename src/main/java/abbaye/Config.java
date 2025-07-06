/* Copyright (C) The Authors 2025 */
package abbaye;

import abbaye.logs.GameLogger;
import abbaye.logs.JulLogger;
import abbaye.logs.NoopLogger;
import abbaye.logs.StdoutLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/** General config class */
public final class Config {
  private static final int SCR_WIDTH = 640;
  private static final int SCR_HEIGHT = 480;
  //  private static final float PLAYER_DECEL = 0.008f;

  private static final String DEFAULT_CONFIG_RESOURCE = "/abbaye.properties";
  private static final float DEFAULT_GRAVITY = 16.0f;
  private static Config instance = null;

  private final Properties properties;
  private int level = 1;
  private int highScore = 0;
  private GameLogger logger = null;
  private Optional<Boolean> oHeadless = Optional.empty();

  /** Empty properties constructor */
  private Config() {
    properties = new Properties();
  }

  /**
   * Constructor that loads properties from a resource.
   *
   * @param resourcePath
   */
  private Config(String resourcePath) {
    properties = new Properties();
    try (InputStream input = Config.class.getResourceAsStream(resourcePath)) {
      properties.load(input);
    } catch (IOException ex) {
      System.err.println("Error reading the configuration file: " + ex.getMessage());
    }
  }

  /**
   * Constructor that loads properties from a filesystem path.
   *
   * @param filePath
   */
  private Config(Path filePath) {
    properties = new Properties();
    try (InputStream input = Files.newInputStream(filePath)) {
      properties.load(input);
    } catch (IOException ex) {
      System.err.println("Error reading the configuration file: " + ex.getMessage());
    }
  }

  public static synchronized Config config(Optional<String> oFilePath) {
    if (instance != null) {
      throw new IllegalStateException("Config is already set");
    }
    if (oFilePath.isEmpty()) {
      instance = new Config(DEFAULT_CONFIG_RESOURCE);
    } else {
      instance = new Config(Path.of(oFilePath.get()));
    }

    return instance;
  }

  public static synchronized Config config() {
    if (instance == null) {
      instance = new Config(DEFAULT_CONFIG_RESOURCE);
    }
    return instance;
  }

  // Mutation methods

  public void resetLevel() {
    level = 1;
  }

  public void setHighScore(int score) {
    if (score > highScore) {
      highScore = score;
    }
  }

  public void setHeadless(boolean headless) {
    oHeadless = Optional.of(headless);
  }

  // Getters for mutable state

  public int getLevel() {
    return level;
  }

  public int getHighScore() {
    return highScore;
  }

  public GameLogger getLogger() {
    if (logger == null) {
      var levelStr = getString("loglevel", "info");
      var sinkStr = getString("logsink", "noop");
      logger =
          switch (sinkStr) {
            case "stdout" -> new StdoutLogger(GameLogger.LogLevel.of(levelStr));
            case "noop" -> new NoopLogger();
            case "jul" -> new JulLogger(levelStr);
            default -> new StdoutLogger(GameLogger.LogLevel.DEBUG);
          };
    }
    return logger;
  }

  // Config getters

  public int getScreenWidth() {
    return getInt("width", SCR_WIDTH);
  }

  public float getGravity() {
    return getNumber("gravity", DEFAULT_GRAVITY);
  }

  public int getScreenHeight() {
    return getInt("height", SCR_HEIGHT);
  }

  public boolean getFullscreen() {
    return getBoolean("fullscreen", false);
  }

  public boolean getGLActive() {
    if (oHeadless.isPresent()) {
      return !oHeadless.get();
    }
    return getBoolean("glactive", true);
  }

  /**
   * Get the value for a key as an integer
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public int getInt(String key, int defaultValue) {
    for (var tryLevel = level; tryLevel > 0; tryLevel -= 1) {
      var tryKey = key + (level > 1 ? ".level" + tryLevel : "");
      String value = properties.getProperty(tryKey);
      if (value != null) {
        try {
          return Integer.parseInt(value);
        } catch (NumberFormatException e) {
          getLogger().error("Invalid integer value for key: " + key);
        }
      }
    }

    return defaultValue;
  }

  /**
   * Get the value for a key as a float
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public float getNumber(String key, float defaultValue) {
    for (var tryLevel = level; tryLevel > 0; tryLevel -= 1) {
      var tryKey = key + (level > 1 ? ".level" + tryLevel : "");
      String value = properties.getProperty(tryKey);
      if (value != null) {
        try {
          return Float.parseFloat(value);
        } catch (NumberFormatException e) {
          System.err.println("Invalid integer value for key: " + key);
        }
      }
    }
    return defaultValue;
  }

  /**
   * Get the value for a key as a boolean
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    for (var tryLevel = level; tryLevel > 0; tryLevel -= 1) {
      var tryKey = key + (level > 1 ? ".level" + tryLevel : "");

      String value = properties.getProperty(tryKey);
      if (value != null) {
        return Boolean.parseBoolean(value);
      }
    }
    return defaultValue;
  }

  /**
   * Get the value for a key as a String
   *
   * @param key
   * @return
   */
  public String getString(String key, String defaultValue) {
    for (var tryLevel = level; tryLevel > 0; tryLevel -= 1) {
      var tryKey = key + (level > 1 ? ".level" + tryLevel : "");
      String value = properties.getProperty(tryKey);
      if (value != null) {
        return value;
      }
    }
    return defaultValue;
  }

  // Low-level methods for debug

  Set<String> getAllKeys() {
    return properties.stringPropertyNames();
  }

  Properties getProperties() {
    return properties;
  }
}
