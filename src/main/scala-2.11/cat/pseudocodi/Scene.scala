package cat.pseudocodi

import java.awt.event.KeyEvent._
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.image.BufferStrategy
import java.awt.{Color, Font, Frame, GraphicsConfiguration, GraphicsDevice, GraphicsEnvironment}
import java.io.File

import akka.actor.{Actor, ActorRef}
import cat.pseudocodi.GameLoop.GameStarted
import cat.pseudocodi.Scene._

import scala.swing._

/**
  * @author Fede
  */
object Scene {

  case object ShowScene

  case object RedrawScene

}

class Scene extends Actor {

  val paddleW = 12
  val paddleH = 100
  val ballWH = 8

  var bounds: Rectangle = null
  var paddle1, paddle2: Paddle = null
  var ball, initBall: Ball = null

  var gameStarted = false
  var bufferStrategy: BufferStrategy = null

  val mediumFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass.getResource("/arcadeclassic.ttf").getFile)).deriveFont(36f)
  val largeFont = mediumFont.deriveFont(72f)
  var mediumFontHeight, largeFontHeight, titleWidth, startWidth: Int = 0

  var paddle1PressedKey: Option[Int] = None
  var paddle2PressedKey: Option[Int] = None

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => drawGameScreen()
  }

  def showScene(sender: ActorRef) = {
    val env: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
    val device: GraphicsDevice = env.getDefaultScreenDevice
    val gc: GraphicsConfiguration = device.getDefaultConfiguration
    val frame = new Frame(gc)
    frame.setUndecorated(true)
    frame.setIgnoreRepaint(true)
    frame.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = e.getKeyCode match {
        case VK_SPACE => gameStarted = true; sender ! GameStarted
        case VK_W => paddle1PressedKey = Some(VK_W)
        case VK_S => paddle1PressedKey = Some(VK_S)
        case VK_O => paddle2PressedKey = Some(VK_O)
        case VK_K => paddle2PressedKey = Some(VK_K)
        case _ => println("other")
      }

      override def keyReleased(e: KeyEvent) = e.getKeyCode match {
        case VK_W => paddle1PressedKey = None
        case VK_S => paddle1PressedKey = None
        case VK_O => paddle2PressedKey = None
        case VK_K => paddle2PressedKey = None
        case _ => println("other")
      }
    })
    device.setFullScreenWindow(frame)
    bounds = frame.getBounds
    frame.createBufferStrategy(2)
    bufferStrategy = frame.getBufferStrategy
    drawStartScreen(frame, bufferStrategy)

    paddle1 = new Paddle("p1", 50, bounds.height / 2 - (paddleH / 2))
    paddle2 = new Paddle("p2", bounds.width - paddleW - 50, bounds.height / 2 - (paddleH / 2))
    initBall = new Ball(bounds.width / 2 - (ballWH / 2), bounds.height / 2 - (ballWH / 2), 1, 8)
    ball = initBall
  }

  def drawStartScreen(frame: Frame, bufferStrategy: BufferStrategy) = {
    largeFontHeight = frame.getFontMetrics(largeFont).getHeight
    mediumFontHeight = frame.getFontMetrics(mediumFont).getHeight
    titleWidth = frame.getFontMetrics(largeFont).stringWidth("PONG")
    startWidth = frame.getFontMetrics(mediumFont).stringWidth("PRESS SPACE")

    val g = bufferStrategy.getDrawGraphics
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
      g.setFont(largeFont)
      g.drawString("PONG", bounds.width / 2 - (titleWidth / 2), bounds.height / 2 - largeFontHeight / 2)
      g.setFont(mediumFont)
      g.drawString("PRESS SPACE", bounds.width / 2 - (startWidth / 2), bounds.height / 2 + largeFontHeight / 2)

      bufferStrategy.show()
      g.dispose()
    }
  }

  def drawGameScreen() = {
    //COLLISIONS: PADDLE
    paddle1PressedKey.foreach((keyCode: Int) => keyCode match {
      case VK_W => movePaddleUpRequested(paddle1.name)
      case VK_S => movePaddleDownRequested(paddle1.name)
    })
    paddle2PressedKey.foreach((keyCode: Int) => keyCode match {
      case VK_O => movePaddleUpRequested(paddle2.name)
      case VK_K => movePaddleDownRequested(paddle2.name)
    })

    //COLLISIONS: BALL
    if (ball.intersects(paddle1) || ball.intersects(paddle2)) {
      ball = new Ball(ball.x, ball.y, ball.dx * -1, ball.dy)
    } else if (ball.y <= 0 || (ball.y + ball.h) >= bounds.height) {
      ball = new Ball(ball.x, ball.y, ball.dx, ball.dy * -1)
    } else if (ball.x + ball.w < 0 || ball.x > bounds.width) {
      ball = initBall
    }
    ball = ball.move()

    //REPAINT
    val g = bufferStrategy.getDrawGraphics
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
      g.fillRect(paddle1.x, paddle1.y, paddle1.w, paddle1.h)
      g.fillRect(paddle2.x, paddle2.y, paddle2.w, paddle2.h)
      g.fillRect(ball.x, ball.y, ball.w, ball.h)
      bufferStrategy.show()
      g.dispose()
    }
  }

  def movePaddleUpRequested(paddle: String): Unit = {
    if (paddle.equals(paddle1.name)) {
      if (paddle1.y >= 0) paddle1 = paddle1.up()
    } else {
      if (paddle2.y >= 0) paddle2 = paddle2.up()
    }
  }

  def movePaddleDownRequested(paddle: String): Unit = {
    if (paddle.equals(paddle1.name)) {
      if (paddle1.y + paddle1.h < bounds.height) paddle1 = paddle1.down()
    } else {
      if (paddle2.y + paddle2.h < bounds.height) paddle2 = paddle2.down()
    }
  }
}


