
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal



import akka.actor._

object Splash extends js.JSApp {

	def main() = {
		val system = ActorSystem.create("benchmarks")

		VueActor.setSystem(system)

		VueActor.insert(() => new Page(), "page")

		
		//Benchmark
		import benchmark.akka.actor._
		import Chameneos._

		println("--> starting chameneos")
		Chameneos.start = System.currentTimeMillis
    	system.actorOf(Props(new Mall(1000/*000*/, 4)))

	}
}