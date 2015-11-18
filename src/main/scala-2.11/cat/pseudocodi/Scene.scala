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

  override def receive: Receive = {
    case ShowScene => showScene(sender())
    case RedrawScene => mainPane.repaint()
  }

  def showScene(sender: ActorRef) = {
    var player1Pos: PlayerPosition = null
    var player2Pos: PlayerPosition = null
    var gameStarted = false

    new MainFrame {
      title = "PONG: Back to 1972"

      def startBtn = new Button {
        action = Action("Start") {
          gameStarted = true
          mainPane.requestFocus()
          mainPane.requestFocusInWindow()
          player1Pos = new PlayerPosition(mainPane.size.height, mainPane.size.height / 2 - 50)
          player2Pos = new PlayerPosition(mainPane.size.height, mainPane.size.height / 2 - 50)
          sender ! GameLoop.GameStarted
        }
      }

      def gamePane = new Panel {
        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.BLACK)
          if (gameStarted) {
            g.fillRect(0, player1Pos.y, 12, 100) //player 1
            g.fillRect(size.width - 12, player2Pos.y, 12, 100) //player 2
            g.fillRect(size.width / 2 - 4, mainPane.size.height / 2 - 4, 8, 8) //ball
          } else {
            g.fillRect(0, size.height / 2 - 50, 12, 100) //player 1
            g.fillRect(size.width - 12, mainPane.size.height / 2 - 50, 12, 100) //player 2
            g.fillRect(size.width / 2 - 4, mainPane.size.height / 2 - 4, 8, 8) //ball
          }
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
      size = new swing.Dimension(600, 500)
      visible = true
    }
  }
}

case class PlayerPosition(sceneHeight: Int, y: Int) {

  def moveUp(): PlayerPosition = new PlayerPosition(sceneHeight, y - 5)

  def moveDown(): PlayerPosition = new PlayerPosition(sceneHeight, y + 5)

}


