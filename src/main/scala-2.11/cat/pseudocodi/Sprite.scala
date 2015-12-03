package cat.pseudocodi

import java.awt.Rectangle

/**
  * @author fede
  */
abstract class Sprite {
  def x: Int

  def y: Int

  def w: Int

  def h: Int

  def intersects(p: Sprite): Boolean = {
    toRectangle.intersects(p.toRectangle)
  }

  def toRectangle = {
    new Rectangle(x, y, w, h)
  }
}

case class Ball(x: Int, y: Int, dx: Int, dy: Int) extends Sprite {

  def w: Int = 8

  def h: Int = 8

  def move(): Ball = new Ball(x + (w * dx), y + dy, dx, dy)

}

case class Paddle(name: String, x: Int, y: Int) extends Sprite {

  def w: Int = 12

  def h: Int = 100

  def up(): Paddle = new Paddle(name, x, y - (h / 5))

  def down(): Paddle = new Paddle(name, x, y + (h / 5))

}
