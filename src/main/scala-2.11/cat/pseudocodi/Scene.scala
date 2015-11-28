package cat.pseudocodi

import java.awt.Color
import javax.swing.BorderFactory

import akka.actor.{Actor, ActorRef}
import cat.pseudocodi.Scene._

import scala.swing.BorderPanel.Position._
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
  val paddleW = 12
  val paddleH = 100
  val ballWH = 8

  var mainPane: Panel = null
  var paddle1: Paddle = null
  var paddle2: Paddle = null
  var ball: Ball = null
  var initBall: Ball = null
  var gameStarted = false
  var paneHeight = 0
  var paneWidth = 0

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => redrawScene()
  }

  def showScene(sender: ActorRef) = {

    new MainFrame {

      val gamePane = new Panel {

        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.black)
          g.fillRect(0, 0, size.width, size.height)
          g.setColor(Color.white)
          if (gameStarted) {
            g.fillRect(paddle1.x, paddle1.y, paddle1.w, paddle1.h)
            g.fillRect(paddle2.x, paddle2.y, paddle1.w, paddle1.h)
            g.fillRect(ball.x, ball.y, ball.w, ball.h)
          } else {
            g.fillRect(4, size.height / 2 - (paddleH / 2), paddleW, paddleH)
            g.fillRect(size.width - paddleW - 4, size.height / 2 - (paddleH / 2), paddleW, paddleH)
            g.fillRect(size.width / 2 - (ballWH / 2), size.height / 2 - (ballWH / 2), ballWH, ballWH)
          }

        }
      }

      def startBtn = new Button {
        action = Action("Start") {
          paneHeight = gamePane.size.height
          paneWidth = gamePane.size.width
          gameStarted = true
          mainPane.requestFocus()
          mainPane.requestFocusInWindow()
          paddle1 = new Paddle(4, paneHeight / 2 - (paddleH / 2))
          paddle2 = new Paddle(paneWidth - paddleW - 4, paneHeight / 2 - (paddleH / 2))
          initBall = new Ball(size.width / 2 - (ballWH / 2), paneHeight / 2 - (ballWH / 2), 1, 8)
          ball = initBall
          sender ! GameLoop.GameStarted
        }
      }

      mainPane = new BorderPanel {
        border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        layout(startBtn) = North
        layout(gamePane) = Center
        focusable = true
        listenTo(keys)
        reactions += {
          case KeyPressed(_, Key.W, _, _) => paddle1 = paddle1.up()
          case KeyPressed(_, Key.S, _, _) => paddle1 = paddle1.down()
          case KeyPressed(_, Key.O, _, _) => paddle2 = paddle2.up()
          case KeyPressed(_, Key.K, _, _) => paddle2 = paddle2.down()
        }
      }

      contents = mainPane
      resizable = false
      title = "PONG: Back to 1972"
      size = new swing.Dimension(600, 500)
      visible = true
    }
  }

  def redrawScene() = {
    mainPane.repaint()
    if (ball.intersects(paddle1) || ball.intersects(paddle2)) {
      ball = new Ball(ball.x, ball.y, ball.dx * -1, ball.dy)
    } else if (ball.y <= 0 || ball.y + ball.h >= paneHeight) {
      ball = new Ball(ball.x, ball.y, ball.dx, ball.dy * -1)
    } else if (ball.x + ball.w < 0 || ball.x > paneWidth) {
      ball = initBall
    }
    ball = ball.move()
  }
}


