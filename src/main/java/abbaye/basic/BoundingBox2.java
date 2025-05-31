/* Copyright (C) The Authors 2004-2025 */
package abbaye.basic;

public record BoundingBox2(Vector2 centre, Vector2 size) {

  public float left() {
    return centre.x() - size.x() / 2;
  }

  public float right() {
    return centre.x() + size.x() / 2;
  }

  public float top() {
    return centre.y() - size.y() / 2;
  }

  public float bottom() {
    return centre.y() + size.y() / 2;
  }

  public boolean overlaps(BoundingBox2 other) {
    // Check if two bounding boxes collide
    var l = left();
    var r = right();
    var t = top();
    var b = bottom();
    var ol = other.left();
    var or = other.right();
    var ot = other.top();
    var ob = other.bottom();

    return (l <= or && r >= ol) && (t <= ob && b >= ot);
  }
}
