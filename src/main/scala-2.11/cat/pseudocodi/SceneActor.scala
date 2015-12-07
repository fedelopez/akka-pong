package cat.pseudocodi

import java.awt.event.KeyEvent._
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Color, Font, Frame, _}
import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import cat.pseudocodi.GameLoopActor.GameStarted
import cat.pseudocodi.SceneActor._

import scala.swing.{Graphics2D, Rectangle}
import scala.util.Random

/**
  * @author Fede
  */
object SceneActor {

  case object ShowScene

  case object RedrawScene

}

class SceneActor extends Actor {

  var bounds: Rectangle = null
  var paddle1, paddle2: Paddle = null
  var ball: Ball = null

  var playing = false

  var frame: Frame = null
  val smallFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass.getResource("/arcade_classic.ttf").getFile)).deriveFont(36f)
  val mediumFont = smallFont.deriveFont(72f)
  val largeFont = smallFont.deriveFont(144f)

  var paddle1PressedKey: Option[Int] = None
  var paddle2PressedKey: Option[Int] = None

  val soundActor = context.actorOf(Props[SoundActor])

  override def receive: Receive = {
    case ShowScene => initScene(sender()); drawTitleScreen()
    case RedrawScene => drawGameScene()
  }

  def initScene(sender: ActorRef) = {
    val env: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
    val device: GraphicsDevice = env.getDefaultScreenDevice
    val gc: GraphicsConfiguration = device.getDefaultConfiguration
    frame = new Frame(gc)
    frame.setUndecorated(true)
    frame.setIgnoreRepaint(true)
    frame.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = e.getKeyCode match {
        case VK_SPACE if !playing => playing = true; sender ! GameStarted
        case VK_W if playing => paddle1PressedKey = Some(VK_W)
        case VK_S if playing => paddle1PressedKey = Some(VK_S)
        case VK_O if playing => paddle2PressedKey = Some(VK_O)
        case VK_K if playing => paddle2PressedKey = Some(VK_K)
        case _ => ()
      }

      override def keyReleased(e: KeyEvent) = e.getKeyCode match {
        case VK_W if playing => paddle1PressedKey = None
        case VK_S if playing => paddle1PressedKey = None
        case VK_O if playing => paddle2PressedKey = None
        case VK_K if playing => paddle2PressedKey = None
        case _ => ()
      }
    })
    device.setFullScreenWindow(frame)
    bounds = frame.getBounds
    frame.createBufferStrategy(2)
    paddle1 = new Paddle("p1", bounds.width / 2 - (bounds.width / 3), bounds.height / 2 - Paddle.height / 2)
    paddle2 = new Paddle("p2", bounds.width / 2 + (bounds.width / 3), bounds.height / 2 - Paddle.height / 2)
    ball = initBall(randomSign())
  }

  def drawTitleScreen() = {
    val titleMetrics = fontMetrics("PONG", mediumFont)
    val subtitleMetrics = fontMetrics("PRESS SPACE", smallFont)

    val bufferStrategy = frame.getBufferStrategy
    val g = bufferStrategy.getDrawGraphics
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
      g.setFont(mediumFont)
      g.drawString("PONG", bounds.width / 2 - (titleMetrics._1 / 2), bounds.height / 2 - titleMetrics._2 / 2)
      g.setFont(smallFont)
      g.drawString("PRESS SPACE", bounds.width / 2 - (subtitleMetrics._1 / 2), bounds.height / 2 + subtitleMetrics._2 / 2)

      bufferStrategy.show()
      g.dispose()
    }
  }

  def drawGameScene() = {
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
      soundActor ! SoundActor.Ping
    } else if (ball.y <= 0 || (ball.y + ball.h) >= bounds.height) {
      ball = new Ball(ball.x, ball.y, ball.dx, ball.dy * -1)
      soundActor ! SoundActor.Pong
    } else if (ball.x + ball.w < paddle1.x - 50) {
      soundActor ! SoundActor.Miss
      ball = initBall(-1)
      paddle2 = paddle2.scoreUp()
    } else if (ball.x + ball.w > paddle2.x + paddle2.w + 50) {
      soundActor ! SoundActor.Miss
      ball = initBall(1)
      paddle1 = paddle1.scoreUp()
    }
    ball = ball.move()

    //REPAINT
    val bufferStrategy = frame.getBufferStrategy
    val g = bufferStrategy.getDrawGraphics.asInstanceOf[Graphics2D]
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
      g.fillRect(paddle1.x, paddle1.y, paddle1.w, paddle1.h)
      g.fillRect(paddle2.x, paddle2.y, paddle2.w, paddle2.h)
      g.fillRect(ball.x, ball.y, ball.w, ball.h)
      g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, Array(12f), 0.0f))
      g.drawLine(bounds.width / 2, 0, bounds.width / 2, bounds.height)

      g.setFont(largeFont)
      val paddle1Metrics: (Width, Height) = fontMetrics(paddle1.score.toString, largeFont)
      g.drawString(paddle1.score.toString, bounds.width / 4 - (paddle1Metrics._1 / 2), bounds.height / 4 - paddle1Metrics._2 / 2)
      val paddle2Metrics: (Width, Height) = fontMetrics(paddle2.score.toString, largeFont)
      g.drawString(paddle2.score.toString, bounds.width - bounds.width / 4 - (paddle2Metrics._1 / 2), bounds.height / 4 - paddle2Metrics._2 / 2)

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

  def initBall(dx: Int) = new Ball(bounds.width / 2 - Ball.width / 2, bounds.height / 2 - Ball.height / 2, dx, Random.nextInt(Ball.width * 2) * randomSign())

  def randomSign() = Random.nextBoolean() match {
    case true => 1
    case false => -1
  }


  type Width = Int
  type Height = Int

  def fontMetrics(text: String, font: Font): (Width, Height) = (frame.getFontMetrics(font).stringWidth(text), frame.getFontMetrics(font).getHeight)
}


