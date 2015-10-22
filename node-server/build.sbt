
name := "node-ws"

organization := "eu.unicredit"
	
version := "0.0.1-SNAPSHOT"
	
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
	"akka.js" %%% "akkaactor" % "0.2-SNAPSHOT",
	"eu.unicredit" %%% "algos" % "0.0.1-SNAPSHOT"
)

postLinkJSEnv := NodeJSEnv().value

persistLauncher in Compile := true

enablePlugins(ScalaJSPlugin)

//npm install websocket
