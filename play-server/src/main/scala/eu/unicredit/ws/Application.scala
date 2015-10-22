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


	import eu.unicredit.algos._
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

case class BenchResult(name: String, time: String)

class MyWebSocketActor(out: ActorRef) extends Actor {
  import Application._

  override def preStart() =
  	 broadcast ! Register

  def receive = {
    case msg: String =>
    	val splitted = msg.split(",")
    	if (splitted(0) == "benchmark")
    		context.actorOf(Props(new BenchActor(splitted(1))))
    	else
      		broadcast ! msg
    case MsgToPage(msg) =>
      out ! msg
    case BenchResult(name, time) =>
      out ! name+","+time
  }

  override def postStop() =
  	broadcast ! DeRegister
}
