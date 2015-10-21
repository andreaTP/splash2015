package eu.unicredit.algos

import scala.scalajs.js

import akka.actor._

object Main extends js.JSApp {

	def main() = {
		println("running on node!")

		val system = ActorSystem.create("algos")

		import Chameneos._

		system.actorOf(Props(new Actor {
			val cm = context.actorOf(Props(new ChameneosManager()))

			cm ! Start(100,4)

			def receive = {
				case End(time) =>
					println("ENDED!! "+time)
			}
		}))
	}

}
