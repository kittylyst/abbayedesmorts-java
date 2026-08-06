/* Copyright (C) The Authors 2025-2026 */
package abbaye;

import static org.junit.jupiter.api.Assertions.*;

import abbaye.logs.NoopLogger;
import abbaye.logs.StdoutLogger;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Config. Each test resets the singleton before running so tests are independent.
 */
public class TestConfig {

  /** Reset the Config singleton between tests via reflection. */
  @BeforeEach
  public void resetSingleton() throws Exception {
    Field f = Config.class.getDeclaredField("instance");
    f.setAccessible(true);
    f.set(null, null);
  }

  // ── Default resource loading ──────────────────────────────────────────────

  @Test
  public void defaultConfigLoadsWithoutThrowing() {
    assertDoesNotThrow((org.junit.jupiter.api.function.Executable) Config::config);
  }

  @Test
  public void defaultScreenWidthIsReturned() {
    // abbaye.properties sets width=1200
    assertEquals(1200, Config.config().getScreenWidth());
  }

  @Test
  public void defaultScreenHeightIsReturned() {
    // abbaye.properties sets height=800
    assertEquals(800, Config.config().getScreenHeight());
  }

  @Test
  public void defaultFullscreenIsFalse() {
    assertFalse(Config.config().getFullscreen());
  }

  @Test
  public void defaultGravityIsReturned() {
    // gravity not in abbaye.properties → uses DEFAULT_GRAVITY = 16.0f
    assertEquals(16.0f, Config.config().getGravity(), 1e-6f);
  }

  // ── getInt / getNumber / getBoolean / getString defaults ─────────────────

  @Test
  public void getMissingIntReturnsDefault() {
    assertEquals(42, Config.config().getInt("no.such.key", 42));
  }

  @Test
  public void getMissingFloatReturnsDefault() {
    assertEquals(3.14f, Config.config().getNumber("no.such.key", 3.14f), 1e-6f);
  }

  @Test
  public void getMissingBooleanReturnsFalseDefault() {
    assertFalse(Config.config().getBoolean("no.such.key", false));
  }

  @Test
  public void getMissingBooleanReturnsTrueDefault() {
    assertTrue(Config.config().getBoolean("no.such.key", true));
  }

  @Test
  public void getMissingStringReturnsDefault() {
    assertEquals("hello", Config.config().getString("no.such.key", "hello"));
  }

  // ── Logger initialisation ─────────────────────────────────────────────────

  @Test
  public void loggerIsNotNull() {
    assertNotNull(Config.config().getLogger());
  }

  @Test
  public void loggerIsCachedAcrossCalls() {
    var cfg = Config.config();
    assertSame(
        cfg.getLogger(), cfg.getLogger(), "logger must be the same instance on repeated calls");
  }

  @Test
  public void noopSinkProducesNoopLogger() throws Exception {
    // Construct a Config with logsink=noop via reflection on the private constructor
    Config cfg = newConfigFromProperties("loglevel=info\nlogsink=noop\n");
    assertInstanceOf(NoopLogger.class, cfg.getLogger());
  }

  @Test
  public void stdoutSinkProducesStdoutLogger() throws Exception {
    Config cfg = newConfigFromProperties("loglevel=info\nlogsink=stdout\n");
    assertInstanceOf(StdoutLogger.class, cfg.getLogger());
  }

  @Test
  public void unknownSinkFallsBackToStdoutLogger() throws Exception {
    Config cfg = newConfigFromProperties("loglevel=debug\nlogsink=garbage\n");
    assertInstanceOf(StdoutLogger.class, cfg.getLogger());
  }

  // ── Level and high score mutation ─────────────────────────────────────────

  @Test
  public void defaultLevelIsOne() {
    assertEquals(1, Config.config().getLevel());
  }

  @Test
  public void resetLevelSetsLevelToOne() {
    var cfg = Config.config();
    cfg.resetLevel();
    assertEquals(1, cfg.getLevel());
  }

  @Test
  public void setHighScoreUpdatesWhenHigher() {
    var cfg = Config.config();
    cfg.setHighScore(100);
    assertEquals(100, cfg.getHighScore());
    cfg.setHighScore(50);
    assertEquals(100, cfg.getHighScore(), "lower score must not replace high score");
  }

  @Test
  public void setHighScoreIgnoresLowerValues() {
    var cfg = Config.config();
    cfg.setHighScore(200);
    cfg.setHighScore(199);
    assertEquals(200, cfg.getHighScore());
  }

  // ── Headless override ─────────────────────────────────────────────────────

  @Test
  public void setHeadlessTrueMakesGLActiveReturnFalse() {
    var cfg = Config.config();
    cfg.setHeadless(true);
    assertFalse(cfg.getGLActive());
  }

  @Test
  public void setHeadlessFalseMakesGLActiveReturnTrue() {
    var cfg = Config.config();
    cfg.setHeadless(false);
    assertTrue(cfg.getGLActive());
  }

  // ── getAllKeys / getProperties ─────────────────────────────────────────────

  @Test
  public void getAllKeysIsNotEmpty() {
    assertFalse(Config.config().getAllKeys().isEmpty());
  }

  @Test
  public void getPropertiesContainsWidthKey() {
    assertTrue(Config.config().getProperties().containsKey("width"));
  }

  // ── Singleton guards ──────────────────────────────────────────────────────

  @Test
  public void configWithOptionalEmptyLoadsDefaultResource() {
    var cfg = Config.config(java.util.Optional.empty());
    assertNotNull(cfg);
    assertEquals(1200, cfg.getScreenWidth());
  }

  @Test
  public void secondCallToConfigWithOptionalThrows() {
    Config.config(java.util.Optional.empty());
    assertThrows(IllegalStateException.class, () -> Config.config(java.util.Optional.empty()));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /**
   * Creates a fresh (non-singleton) Config with the given properties content, bypassing the
   * singleton, by using the package-private empty constructor and loading properties directly.
   */
  private static Config newConfigFromProperties(String propertiesContent) throws Exception {
    // Use the private no-arg constructor, then load properties manually
    var ctor = Config.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    Config cfg = ctor.newInstance();

    var props = new java.util.Properties();
    props.load(new java.io.StringReader(propertiesContent));

    Field propsField = Config.class.getDeclaredField("properties");
    propsField.setAccessible(true);
    // Properties is final but we can mutate the existing instance's contents
    ((java.util.Properties) propsField.get(cfg)).putAll(props);

    return cfg;
  }
}
