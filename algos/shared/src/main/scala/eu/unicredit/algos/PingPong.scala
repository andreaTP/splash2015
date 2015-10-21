/*
	inspired from:
	https://github.com/shamsmahmood/savina
*/
package eu.unicredit.algos


import akka.actor._

object PingPong {

  case class Start(msgNum: Long)
  case class End(time: Long)

  class PingPongManager extends Actor {

    def receive = {
      case Start(msgNum) =>
        PingPong.start = System.currentTimeMillis
        val pong = context.actorOf(Props(new Pong()))
        val ping = context.actorOf(Props(new Ping(msgNum, pong)))
        ping ! Pong
        context.become(operative(sender))
    }

    def operative(answareTo: ActorRef): Receive = {
      case end: End =>
        answareTo ! end
        context.become(receive)
    }
  }

  case object Ping
  case object Pong

  var start = 0L
  var end = 0L

  class Ping(var count: Long, pong: ActorRef) extends Actor {

  	def receive = {
  		case Pong =>
  			count -= 1
  			
  			if (count < 0) {
  				PingPong.end = System.currentTimeMillis
  				context.parent ! End(PingPong.end - PingPong.start)
  				pong ! PoisonPill
  				self ! PoisonPill
  			} else
  				pong ! Ping
  	} 
  }

  class Pong extends Actor {
  	def receive = {
  		case Ping =>
  			sender ! Pong
  	}
  }

}