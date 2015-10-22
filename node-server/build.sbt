
name := "node-ws"

organization := "eu.unicredit"
	
version := "0.0.1-SNAPSHOT"
	
scalaVersion := "2.11.7"

postLinkJSEnv := NodeJSEnv().value

persistLauncher in Compile := true

enablePlugins(ScalaJSPlugin)
