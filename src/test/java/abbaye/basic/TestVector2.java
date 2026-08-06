/* Copyright (C) The Authors 2025-2026 */
package abbaye.basic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Headless unit tests for Vector2. No GL/GLFW context required. */
public class TestVector2 {

  // ── ORIGIN constant ──────────────────────────────────────────────────────

  @Test
  public void originIsZeroZero() {
    assertEquals(0f, Vector2.ORIGIN.x(), 1e-6f);
    assertEquals(0f, Vector2.ORIGIN.y(), 1e-6f);
  }

  // ── magnitude ────────────────────────────────────────────────────────────

  @Test
  public void magnitudeOfZeroVectorIsZero() {
    assertEquals(0f, new Vector2(0, 0).magnitude(), 1e-6f);
  }

  @Test
  public void magnitudeOfUnitXIsOne() {
    assertEquals(1f, new Vector2(1, 0).magnitude(), 1e-6f);
  }

  @Test
  public void magnitudeOfUnitYIsOne() {
    assertEquals(1f, new Vector2(0, 1).magnitude(), 1e-6f);
  }

  @Test
  public void magnitudeOf345Triangle() {
    assertEquals(5f, new Vector2(3, 4).magnitude(), 1e-5f);
  }

  @Test
  public void magnitudeIsAlwaysNonNegative() {
    assertTrue(new Vector2(-3, -4).magnitude() >= 0f);
  }

  // ── normalize ────────────────────────────────────────────────────────────

  @Test
  public void normalizedVectorHasMagnitudeOne() {
    var v = new Vector2(3, 4).normalize();
    assertEquals(1f, v.magnitude(), 1e-5f);
  }

  @Test
  public void normalizePreservesDirection() {
    var original = new Vector2(3, 4);
    var norm = original.normalize();
    // Ratio of components must be preserved
    assertEquals(original.x() / original.y(), norm.x() / norm.y(), 1e-5f);
  }

  @Test
  public void normalizeUnitXReturnsUnitX() {
    var v = new Vector2(1, 0).normalize();
    assertEquals(1f, v.x(), 1e-6f);
    assertEquals(0f, v.y(), 1e-6f);
  }

  // ── scale ─────────────────────────────────────────────────────────────────

  @Test
  public void scaleByZeroGivesZeroVector() {
    var v = new Vector2(5, 7).scale(0f);
    assertEquals(0f, v.x(), 1e-6f);
    assertEquals(0f, v.y(), 1e-6f);
  }

  @Test
  public void scaleByOneIsIdentity() {
    var original = new Vector2(3, -4);
    var scaled = original.scale(1f);
    assertEquals(original.x(), scaled.x(), 1e-6f);
    assertEquals(original.y(), scaled.y(), 1e-6f);
  }

  @Test
  public void scaleByTwoDoublesBothComponents() {
    var v = new Vector2(3, -4).scale(2f);
    assertEquals(6f, v.x(), 1e-6f);
    assertEquals(-8f, v.y(), 1e-6f);
  }

  @Test
  public void scaleByNegativeOneNegatesBothComponents() {
    var v = new Vector2(3, -4).scale(-1f);
    assertEquals(-3f, v.x(), 1e-6f);
    assertEquals(4f, v.y(), 1e-6f);
  }

  // ── record equality ───────────────────────────────────────────────────────

  @Test
  public void equalVectorsAreEqual() {
    assertEquals(new Vector2(1, 2), new Vector2(1, 2));
  }

  @Test
  public void differentVectorsAreNotEqual() {
    assertNotEquals(new Vector2(1, 2), new Vector2(3, 4));
  }
}
