package eu.unicredit.algos

import akka.actor._

object Main extends App {

		println("running on jvm!")

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
}
