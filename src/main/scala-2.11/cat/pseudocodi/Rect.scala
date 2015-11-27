package cat.pseudocodi

import java.awt.Rectangle

/**
  * @author fede
  */
abstract class Rect {
  def x: Int

  def y: Int

  def w: Int

  def h: Int

  def intersects(p: Rect): Boolean = {
    new Rectangle(x, y, w, h).intersects(new Rectangle(p.x, p.y, p.w, p.h))
  }
}

case class Ball(x: Int, y: Int, d: Int, w: Int = 8, h: Int = 8) extends Rect {

  def move(): Ball = new Ball(x + (w * d), y + 0, d)

}

case class Paddle(x: Int, y: Int, w: Int = 12, h: Int = 100) extends Rect {

  def up(): Paddle = new Paddle(x, y - 5)

  def down(): Paddle = new Paddle(x, y + 5)

}
