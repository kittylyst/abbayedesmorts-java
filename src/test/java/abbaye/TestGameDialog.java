/* Copyright (C) The Authors 2025-2026 */
package abbaye;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestGameDialog {

  @Test
  public void testCurrentSplashPageFlipsEveryFiveSeconds() {
    assertEquals(0, GameDialog.currentSplashPage(0.0));
    assertEquals(0, GameDialog.currentSplashPage(4.99));
    assertEquals(1, GameDialog.currentSplashPage(5.0));
    assertEquals(1, GameDialog.currentSplashPage(9.99));
    assertEquals(0, GameDialog.currentSplashPage(10.0));
  }
}
