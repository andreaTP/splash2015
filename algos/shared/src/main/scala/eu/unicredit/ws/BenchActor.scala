package eu.unicredit.ws

import akka.actor._

import eu.unicredit.algos._

case class BenchResult(name: String, time: String)

object BenchParams {
	final val chamParams = Seq(
		(10, 4),
		(50, 4),
		(100, 4),
		(200, 4),
		(500, 4))

	final val pingpongParams = Seq(
		(10L),
		(50L),
		(100L),
		(500L),
		(1000L))

	final val pipeParams = Seq(
		(10L),
		(50L),
		(100L),
		(500L),
		(1000L))	
}

class BenchActor(name: String) extends Actor {
	import BenchParams._

	def receive = {
		name match {
			case "chameneos" =>
				cham(
					context.actorOf(Props(new Chameneos.ChameneosManager())),
					chamParams
				)
			case "pingpong" =>
				pingpong(
					context.actorOf(Props(new PingPong.PingPongManager())),
					pingpongParams
				)
			case "pipe" =>
				pipe(
					context.actorOf(Props(new Pipe.PipeManager())),
					pipeParams
				)
			case _ =>
				println("not valid KILLING")
				self ! PoisonPill
				;{case _ => }
		}
	}

	def cham(cm: ActorRef, params: Seq[(Int, Int)]): Receive = {
		if (params.isEmpty) 
			self ! PoisonPill
		else
			cm ! Chameneos.Start(params.head._1, params.head._2)

		{
			case Chameneos.End(time) =>
				context.parent ! BenchResult(name, time.toString)
				context.become(cham(cm, params.tail))
		}
	}

	def pingpong(pp: ActorRef, params: Seq[Long]): Receive = {
		if (params.isEmpty) 
			self ! PoisonPill
		else
			pp ! PingPong.Start(params.head)

		;{
			case PingPong.End(time) =>
				context.parent ! BenchResult(name, time.toString)
				context.become(pingpong(pp, params.tail))
		}
	}

	def pipe(p: ActorRef, params: Seq[Long]): Receive = {
		if (params.isEmpty) 
			self ! PoisonPill
		else
			p ! Pipe.Start(params.head)

		;{
			case Pipe.End(time) =>
				context.parent ! BenchResult(name, time.toString)
				context.become(pipe(p, params.tail))
		}
	}
}
