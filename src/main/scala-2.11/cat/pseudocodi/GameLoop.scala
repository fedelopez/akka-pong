package cat.pseudocodi

import akka.actor.{Actor, Props}

/**
 * @author FedericoL
 */
class GameLoop extends Actor {

  val scene = context.actorOf(Props[Scene])

  scene ! Messages.ShowScene

  override def receive: Receive = {
    case Messages.SceneReady =>
      println("OK, let's start")


  }

}

