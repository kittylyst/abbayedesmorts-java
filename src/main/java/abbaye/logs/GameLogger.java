/* Copyright (C) The Authors 2023-2025 */
package abbaye.logs;

/** A logging facade to integrate with runtime-preferred logging mechanism. */
public sealed interface GameLogger permits JulLogger, NoopLogger, StdoutLogger {

  enum LogLevel {
    DEBUG("debug"),
    INFO("info"),
    WARN("warn"),
    ERROR("error");

    private final String level;

    LogLevel(String level) {
      this.level = level;
    }

    public static LogLevel of(String level) {
      for (var type : LogLevel.values()) {
        if (type.level.equalsIgnoreCase(level)) {
          return type;
        }
      }
      throw new IllegalArgumentException("Invalid log level: " + level);
    }
  }

  void debug(String message);

  void debug(String message, Throwable err);

  void info(String message);

  void error(String message);

  void error(String message, Throwable err);

  void warning(String message);

  void warning(String message, Throwable err);

  LogLevel getMinLevel();
}
