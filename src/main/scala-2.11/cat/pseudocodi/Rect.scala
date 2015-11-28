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

case class Ball(x: Int, y: Int, dx: Int, dy: Int) extends Rect {

  def w: Int = 8

  def h: Int = 8

  def move(): Ball = new Ball(x + (w * dx), y + dy, dx, dy)

}

case class Paddle(x: Int, y: Int) extends Rect {

  def w: Int = 12

  def h: Int = 100

  def up(): Paddle = new Paddle(x, y - (h / 5))

  def down(): Paddle = new Paddle(x, y + (h / 5))

}
