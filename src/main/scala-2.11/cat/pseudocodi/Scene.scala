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
  var gameStarted = false

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => redrawScene()
  }

  def showScene(sender: ActorRef) = {

    new MainFrame {

      val gamePane = new Panel {

        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.BLACK)
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
          gameStarted = true
          mainPane.requestFocus()
          mainPane.requestFocusInWindow()
          paddle1 = new Paddle(4, gamePane.size.height / 2 - (paddleH / 2))
          paddle2 = new Paddle(gamePane.size.width - paddleW - 4, gamePane.size.height / 2 - (paddleH / 2))
          ball = new Ball(size.width / 2 - (ballWH / 2), gamePane.size.height / 2 - (ballWH / 2), 1)
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
          case KeyPressed(_, Key.WSm, _, _) => paddle1 = paddle1.up()
          case KeyPressed(_, Key.S, _, _) => paddle1 = paddle1.down()
          case KeyPressed(_, Key.O, _, _) => paddle2 = paddle2.up()
          case KeyPressed(_, Key.K, _, _) => paddle2 = paddle2.down()
        }
      }

      contents = mainPane
      title = "PONG: Back to 1972"
      size = new swing.Dimension(600, 500)
      visible = true
    }
  }

  def redrawScene() = {
    mainPane.repaint()
    if (ball.intersects(paddle1) || ball.intersects(paddle2)) {
      ball = new Ball(ball.x, ball.y, ball.d * -1)
    }
    ball = ball.move()
  }
}


