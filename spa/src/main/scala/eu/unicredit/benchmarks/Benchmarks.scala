package eu.unicredit.benchmarks

import scala.scalajs.js
import scala.concurrent.duration._
import js.Dynamic.literal
import js.JSConverters._

import akka.actor._

import eu.unicredit.ws._

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

class BenchmarkRunner(title: String) extends VueActor {
	val vueTemplate = s"""
		<div class="row">
      		<h2>$title</h2>
		</div>
	"""

	implicit val dispatcher = context.dispatcher

	def operational =  {
		//val button = context.actorOf(Props(new RunButton()))
		val bbox = context.actorOf(Props(new BenchmarkBox("pipe")))

		vueBehaviour orElse {
			case _ =>
			/*case StartBenchmark =>
				button ! PoisonPill
				val graph = context.actorOf(Props(new BenchmarkBox("pipe")))
				//for (i <- 1 to 15) {
		      //context.system.scheduler.scheduleOnce(i.seconds, graph, Result(i, i * i))
				//}
			*/
		}
	}
}

case object StartPage
case object StartNode
case object StartJvm

case class GraphResult(
  num: Int,
  color: String,
  time: String
)

class BenchmarkBox(name: String) extends VueActor {
	println("Starting bbox ")

	val vueTemplate = s"""
		<div>
			<h2>$name</h2>
			<button type="button" class="btn btn-primary" v-on='click:startPage("${name}")'>Run in page</button>
			<button type="button" class="btn btn-primary" v-on='click:startNode("${name}")'>Run on Node</button>
			<button type="button" class="btn btn-primary" v-on='click:startJvm("${name}")'>Run on Jvm</button>
		</div>
	"""

	override val vueMethods = literal(
		startPage = (n: String) => if (n == name) self ! StartPage,
		startNode = (n: String) => if (n == name) self ! StartNode,
		startJvm = (n: String) => if (n == name) self ! StartJvm)

	def operational = {
		val graph = context.actorOf(Props(new Benchmark()))

		vueBehaviour orElse {
			case StartPage =>
				println("start page")
				context.actorOf(Props(new PageBench(graph)))
			case StartNode =>
				println("start node")
				context.actorOf(Props(new NodeBench(graph)))
			case StartJvm =>
				println("start jvm")
				context.actorOf(Props(new JvmBench(graph)))
			case any =>
				println("Received "+any)
		}
	}

	trait BenchReceiver {
		self: Actor =>
		val graph: ActorRef

		val color: String

		var n: Int = 0

		def receive: Receive = {
			case BenchResult(name, time) =>
				graph ! GraphResult(n, color, time)
				n += 1
				println("run and result is "+time)
		}
	}

	trait WSBenchReceiver {
		self: Actor =>

		val ip: String

		import org.scalajs.dom.raw._

		lazy val ws = new WebSocket(ip)
		override def preStart() = {
    		ws.onmessage = { (event: MessageEvent) =>
    			println("received!! "+event.data.toString)
    			val splitted = event.data.toString.split(",")
    			val from = splitted(0)
    			val txt = splitted(1)

    			self ! BenchResult(from, txt)
    		}
    		ws.onopen = { (event: Event) =>
				ws.send(s"benchmark,$name")
			}
		}

		override def postStop() = {
			ws.close()
		}
	}

	class PageBench(val graph: ActorRef) extends Actor with BenchReceiver {
		val color = "blue"
		context.actorOf(Props(new BenchActor(name)))
	}

	class NodeBench(val graph: ActorRef) extends Actor with BenchReceiver with WSBenchReceiver {
		val color = "red"
		val ip = "ws://localhost:9090"
	}

	class JvmBench(val graph: ActorRef) extends Actor with BenchReceiver with WSBenchReceiver {
		val color = "green"
		val ip = "ws://localhost:9000"
	}
}

class Benchmark extends VueActor {
  var results = Vector.empty[Result]

	val vueTemplate =
		"""
		<svg width="600" height="420">
        	<g transform="translate(80, 40)">
				<line x1="{{xmin - 10}}" y1="{{ymin}}" x2="{{xmax + 10}}" y2="{{ymin}}" stroke="#cccccc" />
				<line x1="{{xmin}}" y1="{{ymin + 10}}" x2="{{xmin}}" y2="{{ymax - 10}}" stroke="#cccccc" />
				<line v-repeat="vdots" x1="{{xmin}}" y1="{{y}}" x2="{{xmax}}" y2="{{y}}" stroke="#eeeeee" />

          		<path d="{{area}}" fill="rgba(120, 129, 194, 0.6)" stroke="none" />
          		<path d="{{line}}" fill="none" stroke="rgb(120, 129, 194)" />

				<g v-repeat="dots" transform="translate({{x}}, {{y}})">
					<circle r="2" x="0" y="0" stroke="#cccccc" fill="white" />
					<text transform="translate(-3, 15)">{{param}}</text>
				</g>
				<g v-repeat="vdots" transform="translate({{x}}, {{y}})">
					<circle r="2" x="0" y="0" stroke="#cccccc" fill="white" />
					<text transform="translate(-20, 0)">{{time}}</text>
				</g>
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
					val timeMin = results.minBy(_.time).time
					val timeMax = results.maxBy(_.time).time
					val xmin = stock.xscale(0)
					val xmax = stock.xscale(results.maxBy(_.param).param)
					val ymin = stock.yscale(0)
					val ymax = stock.yscale(timeMax)
					val dots = results map { r =>
						literal(x = stock.xscale(r.param), y = ymin, param = r.param)
					}
					val vNumDots = 5
					val vDots = (1 to vNumDots) map { c =>
						val time = (timeMin + c * (timeMax - timeMin) / vNumDots).toInt
						literal(x = xmin, y = stock.yscale(time), time = time)
					}
	        		vue.$set("line", curve.line.path.print)
	        		vue.$set("area", curve.area.path.print)
					vue.$set("xmin", xmin)
					vue.$set("xmax", xmax)
					vue.$set("ymin", ymin)
					vue.$set("ymax", ymax)
					vue.$set("dots", dots.toJSArray)
					vue.$set("vdots", vDots.toJSArray)
				}
		}