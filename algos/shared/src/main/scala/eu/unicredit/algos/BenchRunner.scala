
package eu.unicredit.algos

import akka.actor._
import akka.pattern.ask

import scala.concurrent.duration._

object BenchRunner {

	val system = ActorSystem.create("algos")


	def chameneos() = {
		var time = 0L

		import Chameneos._
		

		system.actorOf(Props(new Actor {
			val cm = context.actorOf(Props(new ChameneosManager()))

			cm ! Start(100,4)

			def receive = {
				case "start" =>
					context.become(waitEnd(sender))
			}

			def waitEnd(s: ActorRef): Receive = {
				case End(t) =>
					time = t
					s ! time
					println("ENDED!! "+time)
			}
		})).?("start")(30 seconds)
	}

	def pingpong() = {
		var time = 0L

		import PingPong._

		system.actorOf(Props(new Actor {
			val pm = context.actorOf(Props(new PingPongManager()))		

			def receive = {
				case "start" =>
					pm ! Start(10000)
					context.become(waitEnd(sender))
			}

			def waitEnd(s: ActorRef): Receive = {
				case End(t) =>
					time = t
					s ! time
					println("ENDED!! "+time)
			}
		})).?("start")(30 seconds)
	}
/*		
		import PingPong._

		system.actorOf(Props(new Actor {
			val pm = context.actorOf(Props(new PingPongManager()))

			pm ! Start(10000)

			def receive = {
				case End(time) =>
					println("ENDED!! "+time)
			}
		}))
*/
}