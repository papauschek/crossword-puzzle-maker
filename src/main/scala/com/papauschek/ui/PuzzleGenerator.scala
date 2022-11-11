package com.papauschek.ui

import com.papauschek.puzzle.{Puzzle, PuzzleConfig}
import org.scalajs.dom.{Element, Worker}
import scala.concurrent.{Future, Promise}
import upickle.default.*

/** puzzle generator that is parallelized using Web Workers */
object PuzzleGenerator:
  
  /** use this number of parallel web workers (= CPU threads) for performance */
  private val WORKER_COUNT = 4

  /** configure web workers and collect results */
  private var results = Seq.empty[Puzzle]
  private var promise: Promise[Seq[Puzzle]] = Promise.successful(Nil)
  private val workers = (0 until WORKER_COUNT).map(_ => new Worker("js/worker.js"))
  workers.foreach {
    worker =>
      worker.onmessage = {
        msg =>
          val puzzle = read[Puzzle](msg.data.toString)
          results :+= puzzle
          if (results.length == WORKER_COUNT) {
            promise.success(results)
          }
      }
  }

  /** send a message to the web workers to create a new puzzle with the given settings */
  def send(newPuzzleMessage: NewPuzzleMessage): Future[Seq[Puzzle]] =
    results = Nil
    promise = Promise[Seq[Puzzle]]
    workers.foreach {
      worker => worker.postMessage(write(newPuzzleMessage))
    }
    promise.future

/** a message for the web workers to create a new puzzle with the given settings and words */
case class NewPuzzleMessage(puzzleConfig: PuzzleConfig, words: Seq[String])

object NewPuzzleMessage {

  /** macro for JSON serialization */
  implicit val rw: ReadWriter[NewPuzzleMessage] = macroRW

}