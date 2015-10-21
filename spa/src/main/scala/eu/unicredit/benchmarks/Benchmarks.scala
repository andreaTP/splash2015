package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._
import paths.high.Stock

class BenchmarkPage extends VueActor {

	val vueTemplate =
		"""
			<div class="col-md-12">
				<h1>
					Benchmarks
					<small>
						<span class="glyphicon glyphicon-dashboard"></span>
					</small>
				</h1>
			</div>
		"""

	def operational = {
    val bench = context.actorOf(Props(new Benchmark()), "benchmark")
    for (i <- 1 to 20) {
      bench ! Result(i, i * i)
    }
		vueBehaviour orElse {
			case any => println(s"Benchmarks Received $any")
		}
  }
}

case class Result(
  param: Int,
  time: Double
)

class Benchmark extends VueActor {
  var results = Vector.empty[Result]

	val vueTemplate =
		"""
			<svg width="500" height="400">
        <g transform="translate(40, 40)">
          <path d="{{area}}" fill="#7881C2" stroke="none" />
          <path d="{{line}}" fill="none" stroke="#7881C2" />
        </g>
			</svg>
		"""

	def operational =
		vueBehaviour orElse {
			case r: Result =>
        results :+= r
        val curve = Stock[Result](
            data = List(results),
            xaccessor = _.param,
            yaccessor = _.time,
            width = 420,
            height = 360,
            closed = true
          ).curves.head
        vue.$set("line", curve.line.path.print)
        vue.$set("area", curve.area.path.print)
        println(curve)
		}
}