package eu.unicredit.algos

import akka.actor._
import scala.util.{Success, Failure}

object Main extends App {

		println("running on jvm!")

		import BenchRunner.system._

		BenchRunner.chameneos().onComplete{
			case Success(time) => println("Finished! "+time)
			case Failure(err) => println("error")
		}

/*
		val system = ActorSystem.create("algos")
		import Chameneos._

		system.actorOf(Props(new Actor {
			val cm = context.actorOf(Props(new ChameneosManager()))

			cm ! Start(100,4)

			def receive = {
				case End(time) =>
					println("ENDED!! "+time)
					System.exit(0)
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
