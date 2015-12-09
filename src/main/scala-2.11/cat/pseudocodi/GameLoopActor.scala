package cat.pseudocodi

import akka.actor.{Actor, Cancellable, Props}
import cat.pseudocodi.GameLoopActor._

import scala.concurrent.duration._

/**
  * @author Fede
  */
object GameLoopActor {

  case object TitleScreenRequested

  case object GameStarted

  case object GameFinished

  case object ExitRequested

}

class GameLoopActor extends Actor {

  import context.dispatcher

  var schedule: Option[Cancellable] = None
  val scene = context.actorOf(Props[SceneActor])

  scene ! SceneActor.DrawTitleScene

  override def receive: Receive = {
    case GameLoopActor.GameStarted =>
      cancelSchedule()
      schedule = Some(context.system.scheduler.schedule(Duration.Zero, 20.millis, scene, SceneActor.RedrawScene))
    case GameLoopActor.GameFinished =>
      cancelSchedule()
      schedule = Some(context.system.scheduler.schedule(Duration.Zero, 20.millis, scene, SceneActor.RedrawGameOverScene))
    case TitleScreenRequested =>
      cancelSchedule()
      scene ! SceneActor.DrawTitleScene
    case ExitRequested =>
      cancelSchedule()
      context.system.terminate()
  }

  def cancelSchedule() = schedule.foreach((cancellable: Cancellable) => cancellable.cancel())
}

