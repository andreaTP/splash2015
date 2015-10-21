
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

class Page extends VueActor {
	import MenuMsg._

	val vueTemplate =
		"""<div class="container"><p class="mini-title">My page container and root</p></div>"""

	def operational = {
		val menu = context.actorOf(Props(new Menu()), "menu");
		val pbody = context.actorOf(Props(new PageBody()), "pbody");

		vueBehaviour orElse {
			case cpa: ChangePageApply =>
				pbody !	cpa
		}
	}
}

object MenuMsg {
	case class ChangePageAsk(page: Int)
	case class ChangePageApply(page: Int)
}

class Menu extends VueActor {
	import MenuMsg._

	val vueTemplate =
		"""
			<ul class="nav nav-pills">
				<li v-on='click:selectPage(1)' class="{{c1}}"><a href="#">Page 1</a></li>
				<li v-on='click:selectPage(2)' class="{{c2}}"><a href="#">Page 2</a></li>
				<li v-on='click:selectPage(3)' class="{{c3}}"><a href="#">Page 3</a></li>
			</ul>
		"""

	override val vueMethods = literal(
		selectPage = (x: Int) => self ! ChangePageAsk(x))

	def operational = select(2)

	def select(page: Int): Receive = {
		context.parent ! ChangePageApply(page)

		for (i <- 1 to 3) {
			vue.$set(s"c$i", "")
		}

		vue.$set(s"c$page", "active")

		vueBehaviour orElse {
			case ChangePageAsk(p) =>
				context.become(select(p))
		}
	}
}

class PageBody extends VueActor {
	import MenuMsg._

	val vueTemplate =
		"""<div class="row"><hr /></div>"""

	def operational =
		vueBehaviour orElse {
			case ChangePageApply(p) =>
				context.children.foreach(_ ! PoisonPill)
				p match {
					case 1 =>
						context.actorOf(Props(new TodoPage()))
					case 2 =>
						context.actorOf(Props(new BenchmarkPage()))
					case 3 =>
						context.actorOf(Props(new ChatPage()))
				}
		}
}

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

	def operational =
		vueBehaviour orElse {
			case any => println(s"Benchmarks Received $any")
		}
}
