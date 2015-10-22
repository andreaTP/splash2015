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
			<svg width="500" height="420">
        <g transform="translate(40, 40)">
					<line x1="{{xmin - 10}}" y1="{{ymin}}" x2="{{xmax + 10}}" y2="{{ymin}}" stroke="#cccccc" />
					<line x1="{{xmin}}" y1="{{ymin + 10}}" x2="{{xmin}}" y2="{{ymax - 10}}" stroke="#cccccc" />
          <path d="{{area}}" fill="#7881C2" stroke="none" />
          <path d="{{line}}" fill="none" stroke="#7881C2" />
        </g>
			</svg>
		"""

	def operational =
		vueBehaviour orElse {
			case r: Result =>
        results :+= r
        val stock = Stock[Result](
            data = List(results),
            xaccessor = _.param,
            yaccessor = _.time,
            width = 420,
            height = 360,
            closed = true
          )
				val curve = stock.curves.head
				val xmin = stock.xscale(0)
				val xmax = stock.xscale(results.maxBy(_.param).param)
				val ymin = stock.yscale(0)
				val ymax = stock.yscale(results.maxBy(_.time).time)
        vue.$set("line", curve.line.path.print)
        vue.$set("area", curve.area.path.print)
				vue.$set("xmin", xmin)
				vue.$set("xmax", xmax)
				vue.$set("ymin", ymin)
				vue.$set("ymax", ymax)
		}
}