enablePlugins(ScalaJSPlugin)

name := "crossword-puzzle-maker"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.2.0"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "2.1.0",
  "com.lihaoyi" %%% "upickle" % "2.0.0"
)

// Output Directory for Main Javascript Application
Compile / fastOptJS / artifactPath := baseDirectory.value / "docs/js/main.js"
Compile / fullOptJS / artifactPath := baseDirectory.value / "docs/js/main.js"

