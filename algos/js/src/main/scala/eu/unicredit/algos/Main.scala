package eu.unicredit.algos

import scala.scalajs.js

import akka.actor._

object Main extends js.JSApp {

	def main() = {
		println("running on node!")

		val system = ActorSystem.create("algos")

		import Chameneos._

		Chameneos.start = System.currentTimeMillis
    	system.actorOf(Props(new Mall(1000/*000*/, 4)))
	}
}
