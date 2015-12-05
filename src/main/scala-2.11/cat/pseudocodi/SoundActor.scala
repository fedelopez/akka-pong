package cat.pseudocodi

import javax.sound.sampled.{AudioInputStream, AudioSystem}

import akka.actor.Actor
import cat.pseudocodi.SoundActor._

/**
  * @author fede
  */
object SoundActor {

  case object Ping

  case object Pong

  case object Miss

}

class SoundActor extends Actor {

  def playSound(name: String) = {
    val clip = AudioSystem.getClip()
    val stream: AudioInputStream = AudioSystem.getAudioInputStream(getClass.getResource(s"/$name"))
    clip.open(stream)
    clip.start()
  }

  override def receive: Receive = {
    case Ping => playSound("ping.wav")
    case Pong => playSound("pong.wav")
    case Miss => playSound("miss.wav")
  }

}
