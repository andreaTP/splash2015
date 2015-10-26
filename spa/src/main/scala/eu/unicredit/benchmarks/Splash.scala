
package eu.unicredit.benchmarks

import scala.scalajs.js

import akka.actor._

object Splash extends js.JSApp {

  def main() = {
    val system = ActorSystem.create("benchmarks")

    VueActor.setSystem(system)

    VueActor.insert(() => new Page(), "page")
  }
}