package cat.pseudocodi

import akka.actor.Actor

/**
 * @author FedericoL
 */
class Ball extends Actor {

  override def receive: Receive = ???

}

case class Player(name: String, x: Integer, y: Integer)
