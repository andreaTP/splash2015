
package eu.unicredit.benchmarks

import akka.actor._
import scala.concurrent.Promise
import scala.util._

import com.felstar.scalajs.vue._

import org.scalajs.dom
import scala.scalajs.js
import js.Dynamic.literal


object VueActor {

  case class AddVueChild(v: Vue)

  case object VueChildAdded

  private case class NewVueActor(av: () => VueActor, name: Option[String])

  val promRoot = Promise[ActorRef]
  val futRoot = promRoot.future

  lazy val root = futRoot.value.get.get

  def setSystem(as : ActorSystem) =
    promRoot.success(as.actorOf(Props(VueActor.rootProto()), "root"))

  def insert(va: () => VueActor, name: String) = {
    root ! NewVueActor(va, Some(name))
  }

  def insert(va: () => VueActor, name: Option[String] = None) = {
    root ! NewVueActor(va, name)
  }

  def rootProto = () =>
    new VueActor {
      val vueTemplate = ""

      vue = new Vue(literal(el="#root"))

      override def preStart() = {}

      override def receive =
        vueBehaviour orElse {
          case NewVueActor(av, name) =>
            if (name.isDefined)
              context.actorOf(Props(av()), name.get)
            else
              context.actorOf(Props(av()))
          case any => println("I'm root and I do not wanna answare anyone "+any)
        }

      def operational(): Receive = {case _ =>}
    }
}

trait VueActor extends Actor {
  me =>
  import VueActor._

  val vueTemplate: String

  val vueMethods: js.Dynamic = literal()

  def vueBehaviour: Receive = {
    case AddVueChild(v) =>

      val sonName = sender.path.name.replace("$","")

      val child = dom.document.createElement(sonName)

      vue.$el.appendChild(child)

      vue.$addChild(v)

      vue.$compile(vue.$el)

      sender ! VueChildAdded
  }

   lazy val vueName = self.path.name.replace("$","")

   var vue: Vue = null

  val vueProto: () => Vue = () =>
    Vue.component(vueName, Vue.extend(
    literal(
      ready= (((thisVue: Vue) => {
          me.vue = thisVue
        }): js.ThisFunction),
        template=me.vueTemplate,
        methods=me.vueMethods
    ))).asInstanceOf[Vue]

  override def preStart() = {
    context.parent ! AddVueChild(vueProto())
  }

  override def postStop() = {
    vue.$destroy(true)
  }

  def receive = {
    case VueChildAdded =>
      context.become(operational, true)
    case any =>
      self ! any
  }

  def operational(): Receive
}