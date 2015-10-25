
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

class Page extends VueActor {
  import MenuMsg._

  val vueTemplate =
    """
    <div>
    <nav class="navbar navbar-default">
      <div class="container-fluid">
        <div class="navbar-header">
          <a class="navbar-brand" href="#">
            <img alt="Brand" style="max-height: 30px;"src="./logo.png">
          </a>
        </div>
      </div>
    </nav>
    </div>
    """

  def operational = {
    val menust = context.actorOf(Props(new MenuST()), "menu")
    val pbody = context.actorOf(Props(new PageBody()), "pbody")

    vueBehaviour orElse {
      case cpa: ChangePageApply =>
        pbody !  cpa
    }
  }
}

object MenuMsg {
  case class ChangePageAsk(page: Int)
  case class ChangePageApply(page: Int)
}

class MenuST extends VueScalaTagsActor {
  import MenuMsg._
  import scalatags.Text.all._

  def selectPage(x: Int) =
    self ! ChangePageAsk(x)

  def stTemplate = 
    ul(`class` := "nav nav-pills")(
        li(`class`:="{{c1}}", on := {() => selectPage(1)})(
          a(href := "#")(
            "To do")
        ),
        li(`class`:="{{c2}}", on := {() => selectPage(2)})(
          a(href := "#")(
            "Chat")
        ),
        li(`class`:="{{c3}}", on := {() => selectPage(3)})(
          a(href := "#")(
            "Benchmarks")
        ),
        li(`class`:="{{c4}}", on := {() => selectPage(4)})(
          a(href := "#")(
            "Raft")
        )
      )

  def operational = {
    println("template is "+vueTemplate)
    select(1)
  }

  def select(page: Int): Receive = {
    context.parent ! ChangePageApply(page)

    for (i <- 1 to 4) {
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
    """<div class="container" style="padding-top: 0px;"></div>"""

  def operational =
    vueBehaviour orElse {
      case ChangePageApply(p) =>
        context.children.foreach(_ ! PoisonPill)
        p match {
          case 1 =>
            context.actorOf(Props(new TodoPage()))
          case 2 =>
            context.actorOf(Props(new ChatPage()))
          case 3 =>
            context.actorOf(Props(new BenchmarkPage()))
          case 4 =>
            context.actorOf(Props(new RaftPage()))
        }
    }
}