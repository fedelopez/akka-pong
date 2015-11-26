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
  val paddleWidth = 12
  val paddleHeight = 100
  val ballSquare = 8

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => mainPane.repaint()
  }

  def showScene(sender: ActorRef) = {
    var player1Pos: PlayerPosition = null
    var player2Pos: PlayerPosition = null
    var ballPos: Position = null
    var gameStarted = false

    new MainFrame {

      val gamePane = new Panel {
        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.BLACK)
          if (gameStarted) {
            g.fillRect(player1Pos.x, player1Pos.y, paddleWidth, paddleHeight)
            g.fillRect(player2Pos.x, player2Pos.y, paddleWidth, paddleHeight)
            g.fillRect(ballPos.x, ballPos.y, ballSquare, ballSquare)
          } else {
            g.fillRect(0, size.height / 2 - (paddleHeight / 2), paddleWidth, paddleHeight)
            g.fillRect(size.width - paddleWidth, size.height / 2 - (paddleHeight / 2), paddleWidth, paddleHeight)
            g.fillRect(size.width / 2 - (ballSquare / 2), size.height / 2 - (ballSquare / 2), ballSquare, ballSquare)
          }
        }
      }

      def startBtn = new Button {
        action = Action("Start") {
          gameStarted = true
          mainPane.requestFocus()
          mainPane.requestFocusInWindow()
          player1Pos = new PlayerPosition(0, gamePane.size.height / 2 - (paddleHeight / 2))
          player2Pos = new PlayerPosition(gamePane.size.width - paddleWidth, gamePane.size.height / 2 - (paddleHeight / 2))
          ballPos = new Position(size.width / 2 - (ballSquare / 2), gamePane.size.height / 2 - (ballSquare / 2))
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
          case KeyPressed(_, Key.W, _, _) => player1Pos = player1Pos.moveUp()
          case KeyPressed(_, Key.S, _, _) => player1Pos = player1Pos.moveDown()
          case KeyPressed(_, Key.O, _, _) => player2Pos = player2Pos.moveUp()
          case KeyPressed(_, Key.K, _, _) => player2Pos = player2Pos.moveDown()
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

case class PlayerPosition(x: Int, y: Int) extends Point {

  def moveUp(): PlayerPosition = new PlayerPosition(x, y - 5)

  def moveDown(): PlayerPosition = new PlayerPosition(x, y + 5)

}

