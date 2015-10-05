package cat.pseudocodi

import java.awt.Color
import javax.swing.BorderFactory

import akka.actor.{Actor, ActorRef, Props}

import scala.swing.BorderPanel.Position._
import scala.swing._
import scala.swing.event._

/**
 * @author FedericoL
 */
class Scene extends Actor {

  val ping = context.actorOf(Props[Player], "ping")
  val pong = context.actorOf(Props[Player], "pong")
  val ball = context.actorOf(Props[Ball])

  override def receive: Receive = {
    case Messages.ShowScene => showScene(sender())
    case Messages.RedrawScene => println("Redraw")
  }

  def showScene(sender: ActorRef) = {
    println("Showing frame...")
    new MainFrame {
      title = "PONG: Back to 1972"

      def startBtn = new Button {
        action = Action("Start") {
          sender ! Messages.GameStarted
        }
      }

      def gamePane = new Panel {
        override def paintComponent(g: Graphics2D) {
          g.setColor(Color.BLACK)
          g.fillRect(0, size.height / 2 - 25, 12, 100) //player 1
          g.fillRect(size.width - 12, size.height / 2 - 25, 12, 100) //player 2
          g.fillRect(size.width / 2 - 4, size.height / 2 - 4, 8, 8) //ball
        }
      }

      def mainPane = new BorderPanel {
        border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        layout(startBtn) = North
        layout(gamePane) = Center
        focusable = true
        listenTo(keys)
        reactions += {
          case KeyPressed(_, Key.W, _, _) => println("Move Player 1 UP")
          case KeyPressed(_, Key.S, _, _) => println("Move Player 1 DOWN")
          case KeyPressed(_, Key.O, _, _) => println("Move Player 2 UP")
          case KeyPressed(_, Key.K, _, _) => println("Move Player 2 DOWN")
        }
      }

      contents = mainPane
      size = new swing.Dimension(600, 500)
      visible = true

    }
  }
}
