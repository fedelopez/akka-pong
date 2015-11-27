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

  var mainPane: Panel = null
  val paddleW = 12
  val paddleH = 100
  val ballWH = 8

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => mainPane.repaint()
  }

  def showScene(sender: ActorRef) = {
    var paddle1Pos: PaddlePosition = null
    var paddle2Pos: PaddlePosition = null
    var ballPos: Position = null
    var gameStarted = false

    new MainFrame {

      val gamePane = new Panel {

        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.BLACK)
          g.fillRect(0, 0, size.width, size.height)
          g.setColor(Color.white)
          if (gameStarted) {
            g.fillRect(paddle1Pos.x, paddle1Pos.y, paddleW, paddleH)
            g.fillRect(paddle2Pos.x, paddle2Pos.y, paddleW, paddleH)
            g.fillRect(ballPos.x, ballPos.y, ballWH, ballWH)
            ballPos = new Position(ballPos.x + 8, ballPos.y + 2)
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
          paddle1Pos = new PaddlePosition(4, gamePane.size.height / 2 - (paddleH / 2))
          paddle2Pos = new PaddlePosition(gamePane.size.width - paddleW - 4, gamePane.size.height / 2 - (paddleH / 2))
          ballPos = new Position(size.width / 2 - (ballWH / 2), gamePane.size.height / 2 - (ballWH / 2))
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
          case KeyPressed(_, Key.W, _, _) => paddle1Pos = paddle1Pos.moveUp()
          case KeyPressed(_, Key.S, _, _) => paddle1Pos = paddle1Pos.moveDown()
          case KeyPressed(_, Key.O, _, _) => paddle2Pos = paddle2Pos.moveUp()
          case KeyPressed(_, Key.K, _, _) => paddle2Pos = paddle2Pos.moveDown()
        }
      }

      contents = mainPane
      title = "PONG: Back to 1972"
      size = new swing.Dimension(600, 500)
      visible = true
    }
  }
}

abstract class Point {
  def x: Int

  def y: Int
}

case class Position(x: Int, y: Int) extends Point

case class PaddlePosition(x: Int, y: Int) extends Point {

  def moveUp(): PaddlePosition = new PaddlePosition(x, y - 5)

  def moveDown(): PaddlePosition = new PaddlePosition(x, y + 5)

}

