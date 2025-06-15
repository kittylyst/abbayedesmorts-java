/* Copyright (C) The Authors 2004-2025 */
package abbaye.basic;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

import abbaye.Config;

public final class Clock {
  private static double frameInterval = 0;
  private static double frameTime;
  private static int fps = 0;

  private static double lastTime = 0;
  private static int fpsCounter = 0;

  public static void init() {
    frameTime = glfwGetTime() * 1000;
    Config.config().getLogger().debug("Timer initialized, timer: " + frameTime);
  }

  public static void updateTimer() {
    // The current value of the hires timer, in ticks
    var currentTimeMillis = glfwGetTime() * 1000;

    setFrameInterval(currentTimeMillis - lastTime);
    setFrameTime(currentTimeMillis);

    fpsCounter += 1;
    lastTime = currentTimeMillis;
    //    setFps(fpsCounter);
    //    fpsCounter = 0;
    Config.config().getLogger().debug("FPS: " + fps);
  }

  public static double getFrameInterval() {
    return frameInterval;
  }

  public static void setFrameInterval(double frameInterval) {
    Clock.frameInterval = frameInterval;
  }

  public static double getFrameTime() {
    return frameTime;
  }

  public static void setFrameTime(double frameTime) {
    Clock.frameTime = frameTime;
  }

  public static int getFps() {
    return fps;
  }

  public static void setFps(int fps) {
    Clock.fps = fps;
  }
}
