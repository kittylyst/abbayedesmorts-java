/* Copyright (C) The Authors 2025-2026 */
package abbaye.basic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Headless unit tests for BoundingBox2. No GL/GLFW context required. */
public class TestBoundingBox2 {

  private static BoundingBox2 box(float cx, float cy, float w, float h) {
    return new BoundingBox2(new Vector2(cx, cy), new Vector2(w, h));
  }

  // ── Edge methods ─────────────────────────────────────────────────────────

  @Test
  public void leftIscentreMinusHalfWidth() {
    var b = box(10, 20, 6, 4);
    assertEquals(7.0f, b.left(), 1e-6f);
  }

  @Test
  public void rightIsCentrePlusHalfWidth() {
    var b = box(10, 20, 6, 4);
    assertEquals(13.0f, b.right(), 1e-6f);
  }

  @Test
  public void topIsCentreMinusHalfHeight() {
    var b = box(10, 20, 6, 4);
    assertEquals(18.0f, b.top(), 1e-6f);
  }

  @Test
  public void bottomIsCentrePlusHalfHeight() {
    var b = box(10, 20, 6, 4);
    assertEquals(22.0f, b.bottom(), 1e-6f);
  }

  // ── overlaps — clearly overlapping ───────────────────────────────────────

  @Test
  public void identicalBoxesOverlap() {
    var b = box(0, 0, 10, 10);
    assertTrue(b.overlaps(b));
  }

  @Test
  public void partiallyOverlappingBoxesOverlap() {
    var a = box(0, 0, 10, 10); // x: -5..5, y: -5..5
    var b = box(4, 0, 10, 10); // x: -1..9, y: -5..5
    assertTrue(a.overlaps(b));
    assertTrue(b.overlaps(a));
  }

  @Test
  public void containedBoxOverlaps() {
    var outer = box(0, 0, 20, 20);
    var inner = box(0, 0, 4, 4);
    assertTrue(outer.overlaps(inner));
    assertTrue(inner.overlaps(outer));
  }

  // ── overlaps — touching edges (inclusive boundary) ───────────────────────

  @Test
  public void boxesTouchingAtRightLeftEdgeOverlap() {
    // a right edge == b left edge (both at x=5)
    var a = box(0, 0, 10, 10); // right=5
    var b = box(10, 0, 10, 10); // left=5
    assertTrue(a.overlaps(b), "touching-edge boxes should overlap (inclusive)");
  }

  @Test
  public void boxesTouchingAtBottomTopEdgeOverlap() {
    var a = box(0, 0, 10, 10); // bottom=5
    var b = box(0, 10, 10, 10); // top=5
    assertTrue(a.overlaps(b), "touching-edge boxes should overlap (inclusive)");
  }

  // ── overlaps — separated ─────────────────────────────────────────────────

  @Test
  public void boxesSeparatedHorizontallyDoNotOverlap() {
    var a = box(0, 0, 4, 4); // x: -2..2
    var b = box(10, 0, 4, 4); // x: 8..12
    assertFalse(a.overlaps(b));
    assertFalse(b.overlaps(a));
  }

  @Test
  public void boxesSeparatedVerticallyDoNotOverlap() {
    var a = box(0, 0, 4, 4); // y: -2..2
    var b = box(0, 10, 4, 4); // y: 8..12
    assertFalse(a.overlaps(b));
    assertFalse(b.overlaps(a));
  }

  @Test
  public void boxesSeparatedDiagonallyDoNotOverlap() {
    var a = box(0, 0, 4, 4);
    var b = box(100, 100, 4, 4);
    assertFalse(a.overlaps(b));
  }

  // ── record equality ───────────────────────────────────────────────────────

  @Test
  public void equalBoxesAreEqual() {
    var a = box(1, 2, 3, 4);
    var b = box(1, 2, 3, 4);
    assertEquals(a, b);
  }

  @Test
  public void differentBoxesAreNotEqual() {
    var a = box(1, 2, 3, 4);
    var b = box(5, 6, 3, 4);
    assertNotEquals(a, b);
  }
}
