
package eu.unicredit.algos

import akka.actor._
import akka.pattern.ask

import scala.concurrent.duration._

object BenchRunner {

	val system = ActorSystem.create("algos")


	def chameneos(n: Int = 100, c: Int = 4) = {
		var time = 0L

		import Chameneos._
		

		system.actorOf(Props(new Actor {
			val cm = context.actorOf(Props(new ChameneosManager()))

			cm ! Start(n, c)

			def receive = {
				case "start" =>
					context.become(waitEnd(sender))
			}

			def waitEnd(s: ActorRef): Receive = {
				case End(t) =>
					time = t
					s ! time
			}
		})).?("start")(60 seconds)
	}

	def pingpong(n: Long = 1000) = {
		var time = 0L

		import PingPong._

		system.actorOf(Props(new Actor {
			val pm = context.actorOf(Props(new PingPongManager()))		

			def receive = {
				case "start" =>
					pm ! Start(n)
					context.become(waitEnd(sender))
			}

			def waitEnd(s: ActorRef): Receive = {
				case End(t) =>
					time = t
					s ! time
			}
		})).?("start")(60 seconds)
	}

	def pipe(n: Long = 1000) = {
		var time = 0L

		import Pipe._

		system.actorOf(Props(new Actor {
			val p = context.actorOf(Props(new PipeManager()))		

			def receive = {
				case "start" =>
					p ! Start(n)
					context.become(waitEnd(sender))
			}

			def waitEnd(s: ActorRef): Receive = {
				case End(t) =>
					time = t
					s ! time
			}
		})).?("start")(60 seconds)
	}
}