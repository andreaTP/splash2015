
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
    <!--<div class="container">
      <p class="mini-title">My page container and root</p>
    </div>-->
    </div>"""

  def operational = {
    val menu = context.actorOf(Props(new Menu()), "menu")
    //val menust = context.actorOf(Props(new MenuST()), "menu")
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

class Menu extends VueActor {
  import MenuMsg._

  val vueTemplate =
    """
      <ul class="nav nav-pills">
        <li v-on='click:selectPage(1)' class="{{c1}}"><a href="#">To do</a></li>
        <li v-on='click:selectPage(2)' class="{{c2}}"><a href="#">Chat</a></li>
        <li v-on='click:selectPage(3)' class="{{c3}}"><a href="#">Benchmarks</a></li>
        <li v-on='click:selectPage(4)' class="{{c4}}"><a href="#">Raft</a></li>
      </ul>
    """

  override val vueMethods = literal(
    selectPage = (x: Int) => self ! ChangePageAsk(x))

  def operational = select(1)

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

/*
class MenuST extends VueActor {
  import MenuMsg._

  import scalatags.Text.all._
  import scalatags.text._

  val on = "v-on".attr
/*
  val methods = 
    new scala.collection.mutable.HashMap[String, () => Unit]()
*/
  implicit def registerLambdas: AttrValue[() => Unit] = new AttrValue[() => Unit]{
    def apply(t: Builder, a: Attr, v: () => Unit) = {
      import java.util.UUID

      val uuid = "method"+UUID.randomUUID().toString.replace("-","")
      //methods += (uuid -> f)

      println("REGISTERING -->"+uuid)
      vueMethods.updateDynamic(
      uuid)(v)
      println("vueMethods "+vueMethods)

      t.setAttr(a.name, "click:"+uuid+"()")
  }
    
  }

  def selectPage(x: Int) = {
    println("-----> CALLED!")
    self ! ChangePageAsk(x)
  }

  val stTemplate = 
    ul(
        li(`class`:="{{c1}}", on := {() => selectPage(1)})(
          "Somthing else"
        )
      )

   val vueTemplate = stTemplate.render

   //override val vueMethods = 

   /*
  val vueTemplate =
    """
      <ul class="nav nav-pills">
        <li v-on='click:selectPage(1)' class="{{c1}}"><a href="#">To do</a></li>
        <li v-on='click:selectPage(2)' class="{{c2}}"><a href="#">Chat</a></li>
        <li v-on='click:selectPage(3)' class="{{c3}}"><a href="#">Benchmarks</a></li>
        <li v-on='click:selectPage(4)' class="{{c4}}"><a href="#">Raft</a></li>
      </ul>
    """

  override val vueMethods = literal(
    selectPage = (x: Int) => self ! ChangePageAsk(x))
*/

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
*/
/*
class MenuST extends VueScalaTagsActor {
  import MenuMsg._

//  import VueScalaTagsActor.registerLambdas
  import scalatags.Text.all._

  def selectPage(x: Int) =
    self ! ChangePageAsk(x)

  val stTemplate = 
    ul(
        li(`class`:="{{c1}}", on := {() => selectPage(1)})(
          "To do"
        )/*,
        li(`class`:="{{c2}}", on := {() => selectPage(1)})(
          "Chat"
        ),
        li(`class`:="{{c3}}", on := {() => selectPage(1)})(
          "Benchmarks"
        ),
        li(`class`:="{{c4}}", on := {() => selectPage(1)})(
          "Raft"
        )*/
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
*/
class PageBody extends VueActor {
  import MenuMsg._

  val vueTemplate =
    """
    <div class="container" style="padding-top: 0px;">
      <!--<div class="row"><hr /></div>-->
    </div>"""

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