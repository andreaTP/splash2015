
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

class Page extends VueActor {
	import MenuMsg._	

	val vueTemplate =
		"""<div><p>My page container and root</p></div>"""

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
			<div>
				<button v-on='click:selectPage(1)'>Page 1 {{p1}}</button>
				<button v-on='click:selectPage(2)'>Page 2 {{p2}}</button>
				<button v-on='click:selectPage(3)'>Page 3 {{p3}}</button>
			</div>
		"""

	override val vueMethods = literal( 
		selectPage = (x: Int) => self ! ChangePageAsk(x))

	def operational = select(3)

	def select(page: Int): Receive = {
		context.parent ! ChangePageApply(page)

		for (i <- 1 to 3)
			vue.$set(s"p$i", "")

		vue.$set(s"p$page", "selected")

		vueBehaviour orElse {
			case ChangePageAsk(p) =>
				context.become(select(p))
		}
	}
}

class PageBody extends VueActor {
	import MenuMsg._	

	val vueTemplate =
		"""<div></div>"""

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
			<h1>Benchmarks</h1>
		"""

	def operational = 
		vueBehaviour orElse {
			case any => println(s"Benchmarks Received $any")
		}
}
