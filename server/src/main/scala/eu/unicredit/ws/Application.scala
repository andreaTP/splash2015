package eu.unicredit.ws

import play.api.mvc._
import play.api.Play.current
import akka.actor._

object Application {

	lazy val broadcast = 
		current.actorSystem.actorOf(Props(new MyWebSocketBroadcast()), "broadcast")

	def socket = WebSocket.acceptWithActor[String, String] { request => out =>
  		MyWebSocketActor.props(out)
	}


	import benchmark.akka.actor._
	import Chameneos._

	Chameneos.start = System.currentTimeMillis
    current.actorSystem.actorOf(Props(new Mall(1000000, 4)))

}

case object Register
case object DeRegister
case class MsgToPage(str: String)

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketBroadcast extends Actor {

	def receive = operational(List())

	def operational(l: List[ActorRef]): Receive = {
		case Register =>
			context.become(operational(l :+ sender))
		case DeRegister =>
			context.become(operational(l.filterNot(_ == sender)))
		case msg: String =>
			l.foreach(_ ! MsgToPage(msg))
	}
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  import Application._

  override def preStart() =
  	 broadcast ! Register

  def receive = {
    case msg: String =>
      broadcast ! msg
    case MsgToPage(msg) =>
      out ! msg
  }

  override def postStop() =
  	broadcast ! DeRegister
}
