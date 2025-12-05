enablePlugins(ScalaJSPlugin)

name := "crossword-puzzle-maker"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.2.0"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "2.1.0",
  "com.lihaoyi" %%% "upickle" % "2.0.0",
  "org.scalatest" %%% "scalatest" % "3.2.19" % Test
)

// Test framework
//Test / jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv()

// Output Directory for Main Javascript Application
Compile / fastOptJS / artifactPath := baseDirectory.value / "docs/js/main.js"
Compile / fullOptJS / artifactPath := baseDirectory.value / "docs/js/main.js"

