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

case object Ball {
  val width = 8
  val height = 8
}

case class Ball(x: Int, y: Int, dx: Int, dy: Int) extends Sprite {

  def w: Int = Ball.width

  def h: Int = Ball.height

  def move(): Ball = new Ball(x + (w * dx), y + dy, dx, dy)

}

case object Paddle {
  val width = 12
  val height = 100
}

case class Paddle(name: String, x: Int, y: Int, bounds: Rectangle, score: Int = 0) extends Sprite {

  def w: Int = Paddle.width

  def h: Int = Paddle.height

  def up(): Paddle = if (y > 0) new Paddle(name, x, y - (h / 5), bounds, score) else this

  def down(): Paddle = if (y + h < bounds.height) new Paddle(name, x, y + (h / 5), bounds, score) else this

  def scoreUp(): Paddle = new Paddle(name, x, y, bounds, score + 1)
}
