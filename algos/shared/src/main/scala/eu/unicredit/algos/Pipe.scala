
package eu.unicredit.algos

import akka.actor._

import scala.annotation.tailrec

object Pipe {

  case class Start(targetSize: Long)
  case class End(time: Long)

  class PipeManager extends Actor {

  	@tailrec
  	private def genChildrens(n: Long, next: ActorRef): ActorRef = {
  		val newOne = context.actorOf(Props(new PipeActor(next)))
  		if (n > 0)
        	genChildrens(n-1, newOne)
        else
        	newOne
  	}

    def receive = {
      case Start(targetSize) =>
        Pipe.start = System.currentTimeMillis
        genChildrens(targetSize, self) ! Token
        context.become(operative(sender))
    }

    def operative(answareTo: ActorRef): Receive = {
   	  case Token =>
   	  	Pipe.end = System.currentTimeMillis
   	  	self ! End(Pipe.end - Pipe.start)
   	  	context.children.foreach(_ ! PoisonPill)
      case end: End =>
        answareTo ! end
        context.become(receive)
    }
  }

  case object Token 
  case object Pong

  var start = 0L
  var end = 0L

  class PipeActor(next: ActorRef) extends Actor {
  	def receive = {
  		case Token =>
  			next ! Token
  	}
  }
}