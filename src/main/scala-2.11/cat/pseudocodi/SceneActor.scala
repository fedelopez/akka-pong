package cat.pseudocodi

import java.awt.event.KeyEvent._
import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Color, Font, Frame, _}
import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import cat.pseudocodi.GameLoopActor._
import cat.pseudocodi.SceneActor._

import scala.swing.{Graphics2D, Rectangle}
import scala.util.Random

/**
  * @author Fede
  */
object SceneActor {

  case object InitScene

  case object DrawTitleScene

  case object RedrawScene

  case object RedrawGameOverScene

}

class SceneActor extends Actor {

  var frame: Frame = null
  var bounds: Rectangle = null
  var paddle1, paddle2: Paddle = null
  var ball: Ball = null
  var playing = false
  var paddle1PressedKey: Option[Int] = None
  var paddle2PressedKey: Option[Int] = None

  val smallFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass.getResource("/arcade_classic.ttf").getFile)).deriveFont(36f)
  val mediumFont = smallFont.deriveFont(72f)
  val largeFont = smallFont.deriveFont(144f)

  var senderActor: ActorRef = null
  val soundActor = context.actorOf(Props[SoundActor])

  override def preStart() = initScene()

  override def receive: Receive = {
    case DrawTitleScene => drawTitleScreen()
    case RedrawScene => drawGameScene()
    case RedrawGameOverScene => drawGameOverScene()
  }

  def initScene() = {
    val env: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
    val device: GraphicsDevice = env.getDefaultScreenDevice
    val gc: GraphicsConfiguration = device.getDefaultConfiguration
    frame = new Frame(gc)
    frame.setUndecorated(true)
    frame.setIgnoreRepaint(true)
    frame.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent) = e.getKeyCode match {
        case VK_SPACE if !playing => playing = true; senderActor ! GameStarted
        case VK_W | VK_S if playing => paddle1PressedKey = Some(e.getKeyCode)
        case VK_O | VK_K if playing => paddle2PressedKey = Some(e.getKeyCode)
        case VK_ESCAPE =>
          if (playing) {
            playing = false
            senderActor ! TitleScreenRequested
          } else {
            frame.dispose()
            senderActor ! ExitRequested
          }
        case _ => println("Wrong key")
      }

      override def keyReleased(e: KeyEvent) = e.getKeyCode match {
        case VK_W | VK_S if playing => paddle1PressedKey = None
        case VK_O | VK_K if playing => paddle2PressedKey = None
        case _ => ()
      }
    })
    device.setFullScreenWindow(frame)
    bounds = frame.getBounds
    frame.createBufferStrategy(2)
  }

  def drawTitleScreen() = {
    playing = false
    senderActor = sender()
    paddle1 = new Paddle("p1", bounds.width / 2 - (bounds.width / 3), bounds.height / 2 - Paddle.height / 2, bounds)
    paddle2 = new Paddle("p2", bounds.width / 2 + (bounds.width / 3), bounds.height / 2 - Paddle.height / 2, bounds)
    ball = initBall(randomSign())

    val title = fontMetrics("PONG", mediumFont)
    val subtitle = fontMetrics("PRESS SPACE", smallFont)

    val bufferStrategy = frame.getBufferStrategy
    val g = bufferStrategy.getDrawGraphics
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
      g.setFont(mediumFont)
      g.drawString("PONG", bounds.width / 2 - (title._1 / 2), bounds.height / 2 - title._2 / 2)
      g.setFont(smallFont)
      g.drawString("PRESS SPACE", bounds.width / 2 - (subtitle._1 / 2), bounds.height / 2 + subtitle._2 / 2)

      bufferStrategy.show()
      g.dispose()
    }
  }

  def drawGameScene() = {
    //COLLISIONS: PADDLE
    paddle1PressedKey.foreach((keyCode: Int) => keyCode match {
      case VK_W => paddle1 = paddle1.up()
      case VK_S => paddle1 = paddle1.down()
    })
    paddle2PressedKey.foreach((keyCode: Int) => keyCode match {
      case VK_O => paddle2 = paddle2.up()
      case VK_K => paddle2 = paddle2.down()
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
    if (paddle1.score == 11 || paddle2.score == 11) {
      sender() ! GameFinished
    }

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

  def drawGameOverScene() = {
    //COLLISIONS: BALL
    if (ball.y < 1 || ball.y + ball.h >= bounds.height) {
      ball = new Ball(ball.x, ball.y, ball.dx, ball.dy * -1)
    } else if (ball.x < 1 || ball.x + ball.w > bounds.width) {
      ball = new Ball(ball.x, ball.y, ball.dx * -1, ball.dy)
    }
    ball = ball.move()

    //REPAINT
    val bufferStrategy = frame.getBufferStrategy
    val g = bufferStrategy.getDrawGraphics.asInstanceOf[Graphics2D]
    if (!bufferStrategy.contentsLost) {
      g.setColor(Color.black)
      g.fillRect(0, 0, bounds.width, bounds.height)
      g.setColor(Color.white)
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

  def initBall(dx: Int) = new Ball(bounds.width / 2 - Ball.width / 2, bounds.height / 2 - Ball.height / 2, dx, Random.nextInt(Ball.width * 2) * randomSign())

  def randomSign() = Random.nextBoolean() match {
    case true => 1
    case false => -1
  }

  type Width = Int
  type Height = Int

  def fontMetrics(text: String, font: Font): (Width, Height) = (frame.getFontMetrics(font).stringWidth(text), frame.getFontMetrics(font).getHeight)
}


