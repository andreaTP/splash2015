package eu.unicredit.algos

import scala.scalajs.js

import scala.util.{Success, Failure}

import akka.actor._

object Main extends js.JSApp {

	def main() = {
		println("running on js!")

		import BenchRunner.system._

		BenchRunner.chameneos().onComplete{
			case Success(time) => println("Chameneos Finished! "+time)
			case Failure(err) => println("chameneos error")
		}

		BenchRunner.pingpong().onComplete{
			case Success(time) => println("PingPong Finished! "+time)
			case Failure(err) => println("pingpong error")
		}

		BenchRunner.pipe().onComplete{
			case Success(time) => println("Pipe Finished! "+time)
			case Failure(err) => println("pipe error")
		}
	}

}
