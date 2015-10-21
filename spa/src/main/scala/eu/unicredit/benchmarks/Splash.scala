
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal



import akka.actor._

object Splash extends js.JSApp {

	def main() = {
		val system = ActorSystem.create("benchmarks")

		VueActor.setSystem(system)

		VueActor.insert(() => new Page(), "page")

	/*	
		//Benchmark
		import eu.unicredit.algos._
		import Chameneos._
		import scala.concurrent.duration._
		import system._

		system.scheduler.scheduleOnce(3 seconds){
			println("--> starting chameneos")
			Chameneos.start = System.currentTimeMillis
    		system.actorOf(Props(new Mall(1000/*000*/, 4)))
    	}
	*/
	}
}