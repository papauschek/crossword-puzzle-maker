package com.papauschek.worker

import com.papauschek.puzzle.{Puzzle, PuzzleConfig}
import com.papauschek.ui.NewPuzzleMessage
import com.papauschek.worker.WorkerGlobal
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSGlobalScope}
import upickle.default.*

@JSExportTopLevel("WorkerMain")
object WorkerMain:

  /** run the Web Worker application */
  @JSExport
  def main(): Unit =
    WorkerGlobal.addEventListener("message", onMessage _ )

  /** when receiving a message, the web worker starts generating a puzzle and sends the best puzzle back when done. */
  def onMessage(msg: dom.MessageEvent): Unit =
    val string = msg.data.toString
    val newPuzzleMessage = read[NewPuzzleMessage](string)
    val puzzle = createPuzzle(newPuzzleMessage.puzzleConfig, newPuzzleMessage.words)
    WorkerGlobal.postMessage(write(puzzle))

  /** create a puzzle with the given config and words.
   * - note: this is a CPU intensive process, as we generate many possible puzzles and select the best one based on density
   * - this process is distributed across multiple web workers to spread the CPU-load and keep the UI responsive. */
  private def createPuzzle(config: PuzzleConfig, mainWords: Seq[String]): Puzzle =
    val minSize = mainWords.maxByOption(_.length).map(_.length).getOrElse(1)
    val minConfig = config.copy(width = config.width.max(minSize), height = config.height.max(minSize))
    val start = System.currentTimeMillis()
    var bestRating = 0.0
    var count = 0
    val puzzles = (0 until 1000).flatMap {
      _ =>
        val puzzles = Puzzle.generate(mainWords.head, mainWords.tail.toList, minConfig)
        val puzzle = puzzles.maxBy(_.density)
        count += 1
        if (puzzle.density > bestRating) {
          bestRating = puzzle.density
        }
        puzzles
    }.sortBy(-_.density)
    val end = System.currentTimeMillis()
    val bestPuzzle = puzzles.head
    println(s"Best density: ${bestPuzzle.density}% - $count - ms: ${end - start}")
    bestPuzzle



/** Web worker native browser support */
@js.native
@JSGlobalScope
object WorkerGlobal extends js.Object:
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any): Unit = js.native
