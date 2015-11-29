package cat.pseudocodi

import java.awt.{Color, Font}
import java.io.File

import akka.actor.{Actor, ActorRef}
import cat.pseudocodi.Scene._

import scala.swing._
import scala.swing.event._

/**
  * @author Fede
  */
object Scene {

  case object ShowScene

  case object RedrawScene

}

class Scene extends Actor {

  val paneWidth = 600
  val paneHeight = 500
  val paddleW = 12
  val paddleH = 100
  val ballWH = 8

  var mainPane: Panel = null
  var paddle1: Paddle = new Paddle(4, paneHeight / 2 - (paddleH / 2))
  var paddle2: Paddle = new Paddle(paneWidth - paddleW - 4, paneHeight / 2 - (paddleH / 2))
  val initBall: Ball = new Ball(paneWidth / 2 - (ballWH / 2), paneHeight / 2 - (ballWH / 2), 1, 8)
  var ball: Ball = initBall
  var gameStarted = false

  val gameFont = Font.createFont(Font.TRUETYPE_FONT, new File(getClass.getResource("/arcadeclassic.ttf").getFile))

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => redrawScene()
  }

  def showScene(sender: ActorRef) = {

    new MainFrame {
      mainPane = new Panel {
        focusable = true
        font = gameFont.deriveFont(36f)

        val fontHeight = peer.getFontMetrics(font).getHeight
        val titleWidth = peer.getFontMetrics(font).stringWidth("PONG")
        val startWidth = peer.getFontMetrics(font).stringWidth("PRESS SPACE")

        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.black)
          g.fillRect(0, 0, size.width, size.height)
          g.setColor(Color.white)
          if (gameStarted) {
            g.fillRect(paddle1.x, paddle1.y, paddle1.w, paddle1.h)
            g.fillRect(paddle2.x, paddle2.y, paddle1.w, paddle1.h)
            g.fillRect(ball.x, ball.y, ball.w, ball.h)
          } else {
            g.drawString("PONG", size.width / 2 - (titleWidth / 2), size.height / 2 - fontHeight / 2)
            g.drawString("PRESS SPACE", size.width / 2 - (startWidth / 2), size.height / 2 + fontHeight / 2)
          }
        }

        listenTo(keys)
        reactions += {
          case KeyPressed(_, Key.Space, _, _) => if (!gameStarted) startGame()
          case KeyPressed(_, Key.W, _, _) => paddle1 = paddle1.up()
          case KeyPressed(_, Key.S, _, _) => paddle1 = paddle1.down()
          case KeyPressed(_, Key.O, _, _) => paddle2 = paddle2.up()
          case KeyPressed(_, Key.K, _, _) => paddle2 = paddle2.down()
        }
      }

      def startGame() = {
        gameStarted = true
        mainPane.requestFocus()
        mainPane.requestFocusInWindow()
        sender ! GameLoop.GameStarted
      }

      contents = mainPane
      resizable = false
      title = "PONG: Back to 1972"
      size = new swing.Dimension(paneWidth, paneHeight + 24)
      visible = true
    }
  }

  def redrawScene() = {
    mainPane.repaint()
    if (ball.intersects(paddle1) || ball.intersects(paddle2)) {
      ball = new Ball(ball.x, ball.y, ball.dx * -1, ball.dy)
    } else if (ball.y <= 0 || (ball.y + ball.h) >= paneHeight) {
      ball = new Ball(ball.x, ball.y, ball.dx, ball.dy * -1)
    } else if (ball.x + ball.w < 0 || ball.x > paneWidth) {
      ball = initBall
    }
    ball = ball.move()
  }
}


