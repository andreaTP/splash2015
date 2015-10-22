
package eu.unicredit.ws

import scala.scalajs.js

object Main extends js.JSApp {

	def main() = {
		
		NodeWs.server.listen(9090, () => {
    		println("Server is listening on 9090")
		})

	}

}