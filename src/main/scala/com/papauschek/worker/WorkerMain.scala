package com.papauschek.worker

import com.papauschek.puzzle.{Puzzle, PuzzleConfig}
import com.papauschek.ui.NewPuzzleMessage
import com.papauschek.worker.WorkerGlobal
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSGlobalScope}
import upickle.default.*

@js.native
@JSGlobalScope
object WorkerGlobal extends js.Object {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any): Unit = js.native
}

@JSExportTopLevel("WorkerMain")
object WorkerMain {

  @JSExport
  def main(): Unit = {
    WorkerGlobal.addEventListener("message", onMessage _ )
  }

  def onMessage(msg: dom.MessageEvent): Unit =
    val string = msg.data.toString
    val newPuzzleMessage = read[NewPuzzleMessage](string)
    val puzzle = createPuzzle(newPuzzleMessage.words, newPuzzleMessage.puzzleConfig)
    WorkerGlobal.postMessage(write(puzzle))


  private def createPuzzle(mainWords: Seq[String], config: PuzzleConfig): Puzzle =
    val minSize = mainWords.maxByOption(_.length).map(_.length).getOrElse(1)
    val minConfig = config.copy(width = config.width.max(minSize), height = config.height.max(minSize))
    val start = System.currentTimeMillis()
    var bestRating = 0.0
    var count = 0
    val puzzles = (0 until 1000).flatMap {
      _ =>
        val puzzles = Puzzle.generate(mainWords.head, mainWords.tail.toList, minConfig)
        val puzzle = puzzles.maxBy(_.rating)
        count += 1
        if (puzzle.rating > bestRating) {
          bestRating = puzzle.rating
        }
        puzzles
    }.sortBy(-_.rating)
    val end = System.currentTimeMillis()
    val bestPuzzle = puzzles.head
    println(s"Best rating: ${bestPuzzle.rating} (${bestPuzzle.density}%) - $count - ms: ${end - start}")
    bestPuzzle


}