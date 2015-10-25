
name := "akka.js-benchmarks"

organization := "eu.unicredit"

scalaVersion := "2.11.7"

version := "0.1-SNAPSHOT"

enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "akka.js" %%% "akkaactor" % "0.2-SNAPSHOT",
  "org.scala-js" %%% "scalajs-dom" % "0.8.1",
  "eu.unicredit" %%% "paths-scala-js" % "0.3.5",
  "eu.unicredit" %%% "algos" % "0.0.1-SNAPSHOT",
  "akka.js" %%% "raft" % "0.2-SNAPSHOT",
  "com.lihaoyi" %%% "scalatags" % "0.5.3"
)

persistLauncher in Compile := true

scalaJSStage in Global := FastOptStage
