
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
          Raft Algorithm
          <small>
            <span class="glyphicon glyphicon-record"></span>
          </small>
      </h1>
      <div>
      	<div class="row">
      		<ul class="nav nav-pills">
        		<li v-on='click:startRaft()'><a href="#">Start</a></li>
      		</ul>
      	</div>
      	<div class="row">
       	<div id="RAFT"></div>
       	</div>
      </div>
    """

  override val vueMethods = literal(
    startRaft = 
    	() => { 
    		js.Dynamic.global.startPlayground()
    		demo.Main.main()
    	}
    )

  def operational = vueBehaviour

}
