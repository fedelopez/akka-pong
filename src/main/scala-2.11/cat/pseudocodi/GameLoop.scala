package cat.pseudocodi

import akka.actor.{Actor, Props}

import scala.concurrent.duration._

/**
 * @author Fede
 */
object GameLoop {

  case object GameStarted

}
class GameLoop extends Actor {

  import context.dispatcher

  val scene = context.actorOf(Props[Scene])

  scene ! Scene.ShowScene

  override def receive: Receive = {
    case GameLoop.GameStarted => context.system.scheduler.schedule(Duration.Zero, 40.millis, scene, Scene.RedrawScene)
  }

}

