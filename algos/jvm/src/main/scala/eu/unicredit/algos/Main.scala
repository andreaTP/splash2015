package eu.unicredit.algos

import akka.actor._
import scala.util.{Success, Failure}

object Main extends App {

	println("running on jvm!")

	import BenchRunner.system._

	BenchRunner.chameneos().onComplete{
		case Success(time) => println("Chameneos Finished! "+time)
		case Failure(err) => println("error")
	}

	BenchRunner.pingpong().onComplete{
		case Success(time) => println("PingPong Finished! "+time)
		case Failure(err) => println("error")
	}

	BenchRunner.pipe().onComplete{
		case Success(time) => println("Pipe Finished! "+time)
		case Failure(err) => println("error")
	}

}
