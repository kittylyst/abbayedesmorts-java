/* Copyright (C) The Authors 2004-2025 */
package abbaye.basic;

import abbaye.Config;
import org.lwjgl.Sys;

public final class Clock {
  private static double frameInterval = 0;
  private static long frameTime = Sys.getTime();
  private static int fps = 0;
  // Ticks per second
  private static final long tmrRes = Sys.getTimerResolution();

  private static long lastTime = 0;
  private static int fpsCounter = 0;

  public static void init() {
    Config.config().getLogger().debug("Timer initialized, timer resolution: " + getTmrRes());
  }

  public static void updateTimer() {
    // The current value of the hires timer, in ticks
    long currentTime = Sys.getTime();

    setFrameInterval(((float) (currentTime - getFrameTime())) / getTmrRes());
    setFrameTime(currentTime);

    fpsCounter += 1;

    if ((currentTime - lastTime) > getTmrRes()) {
      lastTime = currentTime;

      setFps(fpsCounter);
      fpsCounter = 0;
      Config.config().getLogger().debug("FPS: " + fps);
    }
  }

  public static double getFrameInterval() {
    return frameInterval;
  }

  public static void setFrameInterval(float frameInterval) {
    Clock.frameInterval = frameInterval;
  }

  public static long getFrameTime() {
    return frameTime;
  }

  public static void setFrameTime(long frameTime) {
    Clock.frameTime = frameTime;
  }

  public static int getFps() {
    return fps;
  }

  public static void setFps(int fps) {
    Clock.fps = fps;
  }

  public static long getTmrRes() {
    return tmrRes;
  }
}
