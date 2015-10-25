
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

object TodoMsgs {
  case class AddItem(txt: String)
}

class TodoPage extends VueScalaTagsActor {
  import TodoMsgs._
  import scalatags.Text.all._

  def submitTodo() = {
      val str = vue.$get("newtodo").toString
      if (str != "") {
        self ! AddItem(str)
      }
      vue.$set("newtodo", "")
  }

  def stTemplate = div(`class` := "col-md-6")(
      h1(
        "To Do ",
        small(
          span(`class` := "glyphicon glyphicon-th-list")()
        )
      ),
      div(`class` := "input-group")(
        span(`class` := "input-group-addon")(
          span(`class` := "glyphicon glyphicon-pushpin")()
        ),
        input(`type` := "text", 
              `class` := "form-control", 
              "placeholder".attr := "what do you want to do?",
              "v-model".attr := "newtodo",
              on := ("keyup", () => submitTodo, Seq("key 'enter'")))
        ),
      br()(),
      label("Your list:")
    )

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

  class TodoElement(txt: String) extends VueScalaTagsActor {
    import TodoMsgs._
    import scalatags.Text.all._

    def removeItem() =
      self ! PoisonPill

    def stTemplate = li(`class` := "list-group-item")(
      s"$txt",
      span(`class` := "glyphicon glyphicon-remove clickable right",
          on := {() => removeItem})
      )

    def operational = vueBehaviour
  }

}
