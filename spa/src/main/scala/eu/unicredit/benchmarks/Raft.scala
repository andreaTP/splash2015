
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal
import js.Dynamic.global

import akka.actor._

class RaftPage extends VueScalaTagsActor {
  import scalatags.Text.all._

  def startRaft() = { 
    global.startPlayground()
    demo.Main.main()
  }

  def stTemplate =
  	div(cls := "col-md-6")(
  	  h1("Raft Algorithm ")(
        small(
          span(cls := "glyphicon glyphicon-record")
        )
      ),
      div(
        div(cls := "row")(
          ul(cls := "nav nav-pills")(
            li(on := {() => startRaft})(
              a(href := "#")("Start")
            )
          )
        )
      ),
      div(cls := "row")(
        div(id := "RAFT")
      )
  	)

  def operational = vueBehaviour

}
