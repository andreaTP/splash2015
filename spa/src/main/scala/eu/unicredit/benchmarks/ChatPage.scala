
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

object ChatMsgs {
	case class StartChat(name: String)

	case class AddChatMsg(from: String, msg: String)
	case class SendChatMsg(from: String, msg: String)
}

class ChatPage extends VueActor {
	import ChatMsgs._

	val vueTemplate =
		"""
			<div class="col-md-6">
				<h1>
					Chat
					<small>
						<span class="glyphicon glyphicon-list"></span>
					</small>
				</h1>
			</div>
		"""

	def operational = {
		val namein = context.actorOf(Props(new NameInput()))

		vueBehaviour orElse {
			case StartChat(name) =>
				namein ! PoisonPill
				context.become(chatStarted(name))
			case any =>
		}
	}

	def chatStarted(name: String): Receive = {
		val namech = context.actorOf(Props(new NameChoosen(name)))
		val sh = context.actorOf(Props(new SocketHandler(name)))

		vueBehaviour orElse {case any => }
	}

	class NameInput extends VueActor {
		val vueTemplate =
			"""
				<div class="input-group">
					<span class="input-group-addon">
						<span class="glyphicon glyphicon-user"></span>
					</span>
					<input type="text" class="form-control" placeholder="what is your name?" v-model="name" v-on="keyup:nameChoosen | key 'enter'"/>
				</div>
			"""

		override val vueMethods = literal(
			nameChoosen = () => {
				val str = vue.$get("name").toString
				context.parent ! StartChat(str)
			})

		def operational = {
			vueBehaviour orElse {case any =>}
		}
	}

	class NameChoosen(name: String) extends VueActor {
		val vueTemplate =
			s"""<blockquote>$name</blockquote>"""

		def operational = {
			vueBehaviour orElse { case any =>}
		}
	}

	class SocketHandler(name: String) extends VueActor {
		val vueTemplate = "<div></div>"

		import org.scalajs.dom.raw._

		//val ws = new WebSocket("ws://localhost:9090")
		val ws = new WebSocket("ws://localhost:9000")
    	ws.onmessage = { (event: MessageEvent) =>
    		val splitted = event.data.toString.split(",")
    		val from = splitted(0)
    		val txt = splitted(1)

    		self ! AddChatMsg(from, txt)
    	}

		def operational = {
			val mi = context.actorOf(Props(new MsgInput(name)))
			val cb = context.actorOf(Props(new ChatBox()))

			vueBehaviour orElse {
				case acm: AddChatMsg =>
					cb ! acm
				case SendChatMsg(from, txt) =>
					ws.send(from+","+txt)
				case any =>
			}
		}

		override def postStop() = {
			ws.close()
			super.postStop()
		}
	}

	class ChatBox(max: Int = 10) extends VueActor {
		val vueTemplate =
			"""<ul class="list-group chat"></ul>"""

		def operational = lineCounter(Seq())

		def lineCounter(lines: Seq[ActorRef]): Receive = {
			vueBehaviour orElse {
				case AddChatMsg(from, txt) =>
					val newLines =
						if (lines.size >= max) {
							lines.head ! PoisonPill
							lines.tail
						} else lines
					val line = context.actorOf(Props(new MsgLine(from, txt)))
					context.become(lineCounter(newLines :+ line))
				case any => println("have to add msg")
			}
		}

		class MsgLine(from: String, txt: String) extends VueActor {
			val vueTemplate =
				s"""<li class="list-group-item">
					<span class="label label-default">$from</span>
					$txt
				</li>"""

			def operational = {
				vueBehaviour orElse { case any =>}
			}
		}
	}

	class MsgInput(name: String) extends VueActor {
		val vueTemplate =
			"""
				<div class="input-group">
					<span class="input-group-addon">
						<span class="glyphicon glyphicon-pencil"></span>
					</span>
					<input type="text" class="form-control" placeholder="what are you thinking?" v-model="msg" v-on="keyup:sendMsg | key 'enter'"/>
				</div>
			"""

		override val vueMethods = literal(
			sendMsg = () => {
				val str = vue.$get("msg").toString
				context.parent ! SendChatMsg(name, str)
				vue.$set("msg", "")
			})

		def operational = {
			vueBehaviour orElse {case any => }
		}
	}
}
