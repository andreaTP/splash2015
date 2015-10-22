
package eu.unicredit.ws

import scala.scalajs.js
import js.Dynamic.{global => g}
import js.Dynamic.literal
import js.DynamicImplicits._

object NodeWs {
/*
	class WebSocket(params: js.Dynamic) extends js.Object {

	}
*/

	val WebSocketServer = g.require("websocket").server
	val ws = g.require("websocket").server
	//val WebSocketServer: js.Object = g.require("websocket").server.asInstanceOf[js.Object]


	val http: js.Dynamic = g.require("http")

	val server = http.createServer((request: js.Dynamic, response: js.Dynamic) => {
			println("Received request for "+request.url)
			response.writeHead(404)
			response.end
	})


	val wsServer = js.Dynamic.newInstance(WebSocketServer)(literal(
    	httpServer = server,
	    autoAcceptConnections = false
	))

	wsServer.on("request", (request: js.Dynamic) => {
    	println("received ws request "+request.protocol)

    	val connection = request.accept(false, request.origin)
    
    	println("connection accepted")
    	
    	connection.on("message", (message: js.Dynamic) => {
        	println("Received message "+message.utf8Data)
            
        	//this loop back
            connection.send(message.utf8Data)
    	})
    
    	connection.on("close", (reasonCode: js.Dynamic, description: js.Dynamic) => {
        	println(" Peer " + connection.remoteAddress + " disconnected.")
    	})
	})

}