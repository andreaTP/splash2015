
lazy val root = project.in(file(".")).
  aggregate(algosJS, algosJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val algos = crossProject.in(file(".")).
  settings(
	name := "algos",
	organization := "eu.unicredit",
	version := "0.0.1-SNAPSHOT",
	scalaVersion := "2.11.7",
  fork in run := true
  ).
  jvmSettings(
  	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.0"
  ).
  jsSettings(
  	libraryDependencies += "akka.js" %%% "akkaactor" % "0.2-SNAPSHOT",
    postLinkJSEnv := NodeJSEnv().value
  )

lazy val algosJVM = algos.jvm
lazy val algosJS = algos.js
