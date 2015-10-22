package eu.unicredit.benchmarks

import scala.scalajs.js
import scala.concurrent.duration._
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
    val bench = context.actorOf(Props(new BenchmarkRunner("Are we fast yet?")), "benchmark")
		vueBehaviour
  }
}

case class Result(
  param: Int,
  time: Double
)

case object StartBenchmark

class BenchmarkRunner(title: String) extends VueActor {
	val vueTemplate = s"""
		<div class="row">
      <h2>$title</h2>
		</div>
	"""

	implicit val dispatcher = context.dispatcher

	def operational =  {
		val button = context.actorOf(Props(new RunButton()))
		vueBehaviour orElse {
			case StartBenchmark =>
				button ! PoisonPill
				val graph = context.actorOf(Props(new Benchmark()))
				for (i <- 1 to 20) {
		      context.system.scheduler.scheduleOnce(i.seconds, graph, Result(i, i * i))
		    }
			}
	}
}

class RunButton extends VueActor {
	val vueTemplate =
		s"""
			<button type="button" class="btn btn-primary" v-on='click:start()'>Run</button>
		"""

		override val vueMethods = literal(
			start = () => context.parent ! StartBenchmark)

		def operational = vueBehaviour
}

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
				if (results.length > 1) {
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
}