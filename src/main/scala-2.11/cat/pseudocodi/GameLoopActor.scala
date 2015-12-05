package cat.pseudocodi

import akka.actor.{Actor, Props}

import scala.concurrent.duration._

/**
 * @author Fede
 */
object GameLoopActor {

  case object GameStarted

}

class GameLoopActor extends Actor {

  import context.dispatcher

  val scene = context.actorOf(Props[SceneActor])

  scene ! SceneActor.ShowScene

  override def receive: Receive = {
    case GameLoopActor.GameStarted => context.system.scheduler.schedule(Duration.Zero, 20.millis, scene, SceneActor.RedrawScene)
  }

}

