package com.papauschek.api

import com.papauschek.puzzle.{Puzzle, PuzzleConfig, PuzzleWords, Point}
import com.papauschek.ui.{PuzzleGenerator, NewPuzzleMessage}
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scalajs.js.Array as JsArray
import scalajs.js.Promise as JsPromise

/** the API for genarating crossword */
@JSExportTopLevel("CrosswordGenerator")
class CrosswordGenerator:

  private var initialPuzzle: Puzzle = Puzzle.empty(PuzzleConfig())
  private var refinedPuzzle: Puzzle = initialPuzzle

  private var mainInputWords: Seq[String] = Nil

  /** generate the puzzle in the background using web workers and returns as JSON */
  @JSExport("generateCrossword")
  def generateCrosswordJs(words: JsArray[String]): JsPromise[String] =
    generateCrossword(words).toJSPromise

  def generateCrossword(words: JsArray[String]): Future[String] =

    val inputWords: Seq[String] = words.toSeq
    if (inputWords.nonEmpty) {
      mainInputWords = PuzzleWords.sortByBest(inputWords)
      val puzzleConfig = PuzzleConfig(
        // TODO 動的に変更
        width = 12,
        height = 12
      )
      
      val p = PuzzleGenerator.send(NewPuzzleMessage(puzzleConfig, mainInputWords))
      .map {
        puzzles => {
          initialPuzzle = puzzles.maxBy(_.density)
          refinedPuzzle = initialPuzzle
          refinedPuzzle.parseToJSON(mainInputWords)
        }
      }
      return p
    }
    val error = Promise[String]()
    error.future
