
package eu.unicredit.ws

import scala.scalajs.js

object Main extends js.JSApp {

	def main() = {
		println("starting here")


		NodeWs.server.listen(9090, () => {
    		println("Server is listening on 9090")
		})

	}

}