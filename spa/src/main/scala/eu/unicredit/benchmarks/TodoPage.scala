
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
			<div class="col-md-6">
			<h1>
				To do
				<small>
					<span class="glyphicon glyphicon-pencil"></span>
				</small>
			</h1>
			<div class="input-group">
			  <span class="input-group-addon">
					<span class="glyphicon glyphicon-ok"></span>
				</span>
				<input type="text" class="form-control" placeholder="what do you want to do?" v-model="newtodo" v-on="keyup:submitTodo | key 'enter'"/>
			</div>
			</br>
			<label>Your list:</label>
			</div>
		"""

	override val vueMethods = literal(
		submitTodo = () => {
			val str = vue.$get("newtodo").toString
			if (str != "") {
				self ! AddItem(str)
			}
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
			"""<ul class="list-group"></ul>"""

		def operational =
			vueBehaviour orElse {
				case AddItem(txt) =>
					context.actorOf(Props(new TodoElement(txt)))
			}
	}

	class TodoElement(txt: String) extends VueActor {
		import TodoMsgs._

		val vueTemplate =
			s"""<li class="list-group-item">
				$txt
				<span class="glyphicon glyphicon-remove clickable right" v-on='click:removeItem'></span>
			</li>"""

		override val vueMethods = literal(
		removeItem = () => {
			self ! PoisonPill
		})

		def operational = vueBehaviour
	}

}
