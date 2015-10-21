
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

object TodoMsgs {
	case class AddItem(txt: String)
}

class TodoPage extends VueActor {
	import TodoMsgs._

	val vueTemplate =
		"""
			<div>
			<h1>TODO</h1>
			<input type="text" placeholder="what do you want to do?" v-model="newtodo" v-on="keyup:submitTodo | key 'enter'"/>
			{{newtodo}}
			</br>
			<label>Your list:</label>
			</div>
		"""

	override val vueMethods = literal(
		submitTodo = () => {
			val str = vue.$get("newtodo").toString
			self ! AddItem(str)
			vue.$set("newtodo", "")
		})

	def operational = {
		val tde = context.actorOf(Props(new TodoElements()))

		vueBehaviour orElse {
			case ai: AddItem =>
				tde ! ai
			case any =>
				println("TODO received "+any)
		}
	}

	class TodoElements extends VueActor {
		import TodoMsgs._

		val vueTemplate =
			"""<ul></ul>"""

		def operational =
			vueBehaviour orElse {
				case AddItem(txt) =>
					context.actorOf(Props(new TodoElement(txt)))
			}
	}

	class TodoElement(txt: String) extends VueActor {
		import TodoMsgs._

		val vueTemplate =
			s"""<li>$txt<button v-on='click:removeItem'>remove</button></li>"""

		override val vueMethods = literal(
		removeItem = () => {
			self ! PoisonPill
		})

		def operational = vueBehaviour
	}

}
