/* Copyright (C) The Authors 2025 */
package abbaye.logs;

public final class NoopLogger implements GameLogger {

  @Override
  public LogLevel getMinLevel() {
    return LogLevel.DEBUG;
  }

  @Override
  public void debug(String message) {}

  @Override
  public void debug(String message, Throwable err) {}

  @Override
  public void info(String message) {}

  @Override
  public void error(String message) {}

  @Override
  public void error(String message, Throwable err) {}

  @Override
  public void warning(String message) {}

  @Override
  public void warning(String message, Throwable err) {}
}
