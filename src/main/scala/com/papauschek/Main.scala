package com.papauschek

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Button, Div, Input}



object Main:

  private var puzzle: Puzzle = Puzzle.empty(PuzzleConfig())
  private var mainInputWords: Seq[String] = Nil

  private val inputElement = dom.document.getElementById("input")
  private val outputElement = dom.document.getElementById("output1")
  private val resultInfoElement = dom.document.getElementById("result-info")

  private val generateButton = dom.document.getElementById("generate-button").asInstanceOf[Button]

  private val resultWithoutElement = dom.document.getElementById("result-without").asInstanceOf[Input]
  private val resultPartialElement = dom.document.getElementById("result-partial").asInstanceOf[Input]
  private val resultFullElement = dom.document.getElementById("result-full").asInstanceOf[Input]

  private val widthInputElement = dom.document.getElementById("width").asInstanceOf[Input]
  private val heightInputElement = dom.document.getElementById("height").asInstanceOf[Input]

  def main(args: Array[String]): Unit =

    println(Globals.german.length)
    println(Globals.english.length)

    generateSolution()
    generateButton.addEventListener("click", { _ => generateSolution() })

    resultWithoutElement.addEventListener("click", { _ => updateSolution() })
    resultPartialElement.addEventListener("click", { _ => updateSolution() })
    resultFullElement.addEventListener("click", { _ => updateSolution() })


  def generateSolution(): Unit =
    val inputWords = inputElement.innerHTML.linesIterator.map(_.trim.toUpperCase).filter(_.nonEmpty).toSeq
    mainInputWords = WordRating.getBest(inputWords)
    val puzzleConfig = PuzzleConfig(
      width = widthInputElement.valueAsNumber.toInt,
      height = heightInputElement.valueAsNumber.toInt
    )
    puzzle = CrosswordMain.create(mainInputWords, puzzleConfig)
    updateSolution()


  def updateSolution(): Unit =
    val showPartialSolution = resultPartialElement.checked
    val showFullSolution = resultFullElement.checked

    outputElement.innerHTML = HtmlRenderer.renderPuzzle(
      puzzle,
      widthInPixels = outputElement.clientWidth,
      showSolution = showFullSolution,
      showPartialSolution = showPartialSolution)

    val unusedWords = mainInputWords.filterNot(puzzle.words.contains)

    resultInfoElement.innerHTML = HtmlRenderer.renderPuzzleInfo(puzzle, unusedWords)

