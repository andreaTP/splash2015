package eu.unicredit.ws

import akka.actor._

import eu.unicredit.algos._

class BenchActor(name: String) extends Actor {

	val chamParams = Seq(
		(10, 4),
		(50, 4),
		(100, 4),
		(200, 4),
		(500, 4))

	val pingpongParams = Seq(
		(10L),
		(50L),
		(100L),
		(500L),
		(1000L))

	val pipeParams = Seq(
		(10L),
		(50L),
		(100L),
		(500L),
		(1000L))


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
				context.parent ! NodeWs.BenchResult(name, time.toString)
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
				context.parent ! NodeWs.BenchResult(name, time.toString)
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
				context.parent ! NodeWs.BenchResult(name, time.toString)
				context.become(pipe(p, params.tail))
		}
	}
}

