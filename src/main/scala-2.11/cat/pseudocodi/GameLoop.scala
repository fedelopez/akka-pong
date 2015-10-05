package cat.pseudocodi

import akka.actor.{Actor, Props}

import scala.concurrent.duration._

/**
 * @author FedericoL
 */
class GameLoop extends Actor {

  import context.dispatcher

  val scene = context.actorOf(Props[Scene])

  scene ! Messages.ShowScene

  override def receive: Receive = {
    case Messages.GameStarted => context.system.scheduler.schedule(Duration.Zero, 200.milliseconds, scene, Messages.RedrawScene)
  }

}

