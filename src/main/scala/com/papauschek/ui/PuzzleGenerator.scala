package com.papauschek.ui

import com.papauschek.puzzle.{Puzzle, PuzzleConfig}
import org.scalajs.dom.{Element, Worker}
import scala.concurrent.{Future, Promise}
import upickle.default.*

/** puzzle generator that is parallelized using Web Workers */
object PuzzleGenerator {

  private val WORKER_COUNT = 4

  private var results = Seq.empty[Puzzle]
  private var promise: Promise[Seq[Puzzle]] = Promise.successful(Nil)

  private val workers = (0 until WORKER_COUNT).map(_ => new Worker("worker.js"))

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

  def send(newPuzzleMessage: NewPuzzleMessage): Future[Seq[Puzzle]] = {
    results = Nil
    promise = Promise[Seq[Puzzle]]
    workers.foreach {
      worker => worker.postMessage(write(newPuzzleMessage))
    }
    promise.future
  }

}

case class NewPuzzleMessage(puzzleConfig: PuzzleConfig, words: Seq[String])

object NewPuzzleMessage {

  implicit val rw: ReadWriter[NewPuzzleMessage] = macroRW

}