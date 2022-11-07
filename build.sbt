enablePlugins(ScalaJSPlugin)

name := "crossword-puzzle-maker"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.2.0"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "2.1.0"
)
