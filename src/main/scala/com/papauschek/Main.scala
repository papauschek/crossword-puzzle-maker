package com.papauschek

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Button, Div, Input}

/**
 * TODO UI
 * - show words that were not used
 * - show density
 * - allow refinement with dictionaries
 */



object Main:

  private var puzzle: Puzzle = Puzzle.empty(PuzzleConfig())

  private val inputElement = dom.document.getElementById("input")
  private val outputElement = dom.document.getElementById("output1")

  private val generateButton = dom.document.getElementById("generate-button").asInstanceOf[Button]

  private val resultWithoutElement = dom.document.getElementById("result-without").asInstanceOf[Input]
  private val resultPartialElement = dom.document.getElementById("result-partial").asInstanceOf[Input]
  private val resultFullElement = dom.document.getElementById("result-full").asInstanceOf[Input]

  def main(args: Array[String]): Unit =
    //generateSolution()
    generateButton.addEventListener("click", { _ => generateSolution() })

    resultWithoutElement.addEventListener("click", { _ => updateSolution() })
    resultPartialElement.addEventListener("click", { _ => updateSolution() })
    resultFullElement.addEventListener("click", { _ => updateSolution() })

  def generateSolution(): Unit =
    val inputWords = inputElement.innerHTML.linesIterator.map(_.trim.toUpperCase).filter(_.nonEmpty).toSeq
    val mainWords = WordRating.getBest(inputWords)
    puzzle = CrosswordMain.create(mainWords)
    updateSolution()

  def updateSolution(): Unit =
    val showPartialSolution = resultPartialElement.checked
    val showFullSolution = resultFullElement.checked

    val html = HtmlRenderer.render(puzzle,
      widthInPixels = outputElement.clientWidth,
      showSolution = showFullSolution,
      showPartialSolution = showPartialSolution)

    outputElement.innerHTML = html

