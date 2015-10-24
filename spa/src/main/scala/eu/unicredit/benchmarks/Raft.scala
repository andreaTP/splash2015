
package eu.unicredit.benchmarks

import scala.scalajs.js
import js.Dynamic.literal

import akka.actor._

class RaftPage extends VueActor {
  import TodoMsgs._

  val vueTemplate =
    """
      <div class="col-md-6">
      <h1>
        Raft Algorithm - TBD
      </h1>
      <iframe http-url='http://localhost:8000/raft/index.html' />
      </div>
    """

  def operational = vueBehaviour

}
