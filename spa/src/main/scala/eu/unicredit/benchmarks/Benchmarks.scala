package eu.unicredit.benchmarks

import scala.scalajs.js
import scala.concurrent.duration._
import scala.collection.mutable.Buffer
import js.Dynamic.literal
import js.JSConverters._
import js.annotation.JSExport

import akka.actor._

import eu.unicredit.ws._
import eu.unicredit.colors._

import paths.high.Stock

class BenchmarkPage extends VueScalaTagsActor {
  import scalatags.Text.all._

  val opts = Seq("pipe", "pingpong", "chameneos")

  def doBench(s: String): Unit = {
      for (i <- opts) {
        println("s -> "+s+" | i"+i)
        if (s == i)
            vue.$set(s"ben$i", "disabled")
          else
          vue.$set(s"ben$i", "")
      }


      context.children.foreach(_ ! PoisonPill)
      context.actorOf(Props(new BenchmarkRunner(s)))
  }

  def stTemplate = div(`class` := "col-md-12")(
      h1(
        "Benchmarks ",
        small(
          span(`class` := "glyphicon glyphicon-dashboard")()
        )
      ),
      ul(`class` := "nav nav-pills")(
        li( cls := "{{benpipe}}",
            on := {() => doBench("pipe")})(
          a(href := "#")(
            "Pipe")
        ),
        li( cls := "{{benpingpong}}",
            on := {() => doBench("pingpong")})(
          a(href := "#")(
            "PingPong")
        ),
        li( cls := "{{benchameneos}}",
            on := {() => doBench("chameneos")})(
          a(href := "#")(
            "Chameneos")
        )
      )
    )

  def operational = vueBehaviour
}

case class Result(
  param: Int,
  time: Double
)

class BenchmarkRunner(name: String) extends VueActor {
  val vueTemplate = 
    """<div class="row"></div>"""

  def operational =  {
    context.actorOf(Props(new BenchmarkBox(name)))
    
    vueBehaviour
  }
}

case object StartPage
case object StartNode
case object StartJvm

@JSExport
case class GraphResult(
  param: Long,
  color: Color,
  time: Int
) {
  @JSExport def light = string(color.copy(alpha = 0.6))
  @JSExport def dark = string(color)
}

class BenchmarkBox(name: String) extends VueScalaTagsActor {
  import scalatags.Text.all._

  def stTemplate = div(cls := "col-md-12")(
      div( cls := "row")(
        div( cls := "btn-group")(
          button( `type` := "button",
                  cls := "btn btn-primary",
                  on := {() => self ! StartPage}
            )("Run in page"),
          button( `type` := "button",
                  cls := "btn btn-primary",
                  on := {() => self ! StartNode}
            )("Run on Node"),
          button( `type` := "button",
                  cls := "btn btn-primary",
                  on := {() => self ! StartJvm}
            )("Run on Jvm")
          )
        )
    )

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

  import eu.unicredit.ws.BenchParams

  val params: Seq[Long] = name match {
    case "pipe" =>
      BenchParams.pipeParams
    case "pingpong" =>
      BenchParams.pingpongParams
    case "chameneos" =>
      BenchParams.chamParams.map(_._1.toLong)
    case _ =>
      Seq[Long]()
  }

  trait BenchReceiver {
    self: Actor =>
    val graph: ActorRef

    val color: Color

    var n: Int = 0

    def receive: Receive = {
      case BenchResult(name, time) =>
        graph ! GraphResult(params(n), color, time.toInt)
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
          println(s"connection open benchmark,$name")

          import scala.concurrent.duration._
          import context._
          context.system.scheduler.scheduleOnce(500 millis){
            ws.send(s"benchmark,$name")
          }
      }
    }

    override def postStop() = {
      ws.close()
    }
  }

  class PageBench(val graph: ActorRef) extends Actor with BenchReceiver {
    val color = Color(158, 199, 247)
    context.actorOf(Props(new BenchActor(name)))
  }

  class NodeBench(val graph: ActorRef) extends Actor with BenchReceiver with WSBenchReceiver {
    val color = Color(183, 188, 192)
    val ip = "ws://localhost:9090"
  }

  class JvmBench(val graph: ActorRef) extends Actor with BenchReceiver with WSBenchReceiver {
    val color = Color(120, 129, 194)
    val ip = "ws://localhost:9000"
  }
}

class Benchmark extends VueActor {
  var results = Map.empty[Color, Buffer[GraphResult]]

  val vueTemplate = """
      <svg width="600" height="420">
        <g transform="translate(80, 40)">
          <line x1="{{xmin - 10}}" y1="{{ymin}}" x2="{{xmax + 10}}" y2="{{ymin}}" stroke="#cccccc" />
          <line x1="{{xmin}}" y1="{{ymin + 10}}" x2="{{xmin}}" y2="{{ymax - 10}}" stroke="#cccccc" />
          <line v-repeat="vdots" x1="{{xmin}}" y1="{{y}}" x2="{{xmax}}" y2="{{y}}" stroke="#eeeeee" />

          <g v-repeat="curves">
            <path d="{{area.path.print()}}" fill="{{item[0].light}}" stroke="none" />
            <path d="{{line.path.print()}}" fill="none" stroke="{{item[0].dark}}" />
          </g>
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
      case r: GraphResult =>
        if (results.get(r.color).isEmpty) {
          results += (r.color -> Buffer())
        }
        results(r.color) += r
        val allResults = results.values.flatten.toSeq
        if (allResults.length > 1) {
          val stock = Stock[GraphResult](
              data = results.values.toSeq,
              xaccessor = _.param,
              yaccessor = _.time,
              width = 420,
              height = 360,
              closed = true
            )
          val timeMin = allResults.minBy(_.time).time
          val timeMax = allResults.maxBy(_.time).time
          val xmin = stock.xscale(0)
          val xmax = stock.xscale(allResults.maxBy(_.param).param)
          val ymin = stock.yscale(0)
          val ymax = stock.yscale(timeMax)
          val allParams = allResults.map(_.param).distinct
          val dots = allParams map { p =>
            literal(x = stock.xscale(p), y = ymin, param = p)
          }
          val vNumDots = 5
          val vDots = (1 to vNumDots) map { c =>
            val time = (timeMin + c * (timeMax - timeMin) / vNumDots).toInt
            literal(x = xmin, y = stock.yscale(time), time = time)
          }
          
          println(stock.curves)
          vue.$set("curves", stock.curves)
          vue.$set("xmin", xmin)
          vue.$set("xmax", xmax)
          vue.$set("ymin", ymin)
          vue.$set("ymax", ymax)
          vue.$set("dots", dots.toJSArray)
          vue.$set("vdots", vDots.toJSArray)
        }
    }
}