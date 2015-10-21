package eu.unicredit.algos

import scala.scalajs.js

import scala.util.{Success, Failure}

import akka.actor._

object Main extends js.JSApp {

	def main() = {
		println("running on node!")

		import BenchRunner.system._

		BenchRunner.chameneos().onComplete{
			case Success(time) => println("Finished! "+time)
			case Failure(err) => println("error")
		}

//		val system = ActorSystem.create("algos")

		/*
		import Chameneos._

		system.actorOf(Props(new Actor {
			val cm = context.actorOf(Props(new ChameneosManager()))

			cm ! Start(100,4)

			def receive = {
				case End(time) =>
					println("ENDED!! "+time)
			}
		}))
		*/
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

}
