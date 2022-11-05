package com.papauschek

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Button, Div, Input, Select}

import scala.scalajs.js
import scala.scalajs.js.Object.{entries, keys}



object Main:

  private var initialPuzzle: Puzzle = Puzzle.empty(PuzzleConfig())
  private var refinedPuzzle: Puzzle = initialPuzzle

  private var mainInputWords: Seq[String] = Nil

  private val inputElement = dom.document.getElementById("input")
  private val outputPuzzleElement = dom.document.getElementById("output-puzzle")
  private val outputCluesElement = dom.document.getElementById("output-clues")
  private val resultInfoElement = dom.document.getElementById("result-info")

  private val generateButton = dom.document.getElementById("generate-button").asInstanceOf[Button]

  private val resultWithoutElement = dom.document.getElementById("result-without").asInstanceOf[Input]
  private val resultPartialElement = dom.document.getElementById("result-partial").asInstanceOf[Input]
  private val resultFullElement = dom.document.getElementById("result-full").asInstanceOf[Input]

  private val widthInputElement = dom.document.getElementById("width").asInstanceOf[Input]
  private val heightInputElement = dom.document.getElementById("height").asInstanceOf[Input]

  private val languageSelect = dom.document.getElementById("language-select").asInstanceOf[Select]
  private val refineButton = dom.document.getElementById("refine-button").asInstanceOf[Button]

  def main(args: Array[String]): Unit =
    generateSolution()
    generateButton.addEventListener("click", { _ => generateSolution() })
    refineButton.addEventListener("click", { _ => refineSolution() })

    resultWithoutElement.addEventListener("click", { _ => renderSolution() })
    resultPartialElement.addEventListener("click", { _ => renderSolution() })
    resultFullElement.addEventListener("click", { _ => renderSolution() })


  def generateSolution(): Unit =
    val rawInputWords = inputElement.innerHTML.linesIterator.map(normalizeWord).toSeq
    val inputWords = rawInputWords.filter(word => word.nonEmpty && !word.startsWith("#"))
    mainInputWords = WordRating.getBest(inputWords)
    val puzzleConfig = PuzzleConfig(
      width = widthInputElement.valueAsNumber.toInt,
      height = heightInputElement.valueAsNumber.toInt
    )
    initialPuzzle = CrosswordMain.create(mainInputWords, puzzleConfig)
    refinedPuzzle = initialPuzzle
    renderSolution()


  def renderSolution(): Unit =
    val showPartialSolution = resultPartialElement.checked
    val showFullSolution = resultFullElement.checked

    outputPuzzleElement.innerHTML = HtmlRenderer.renderPuzzle(
      refinedPuzzle,
      widthInPixels = outputPuzzleElement.clientWidth,
      showSolution = showFullSolution,
      showPartialSolution = showPartialSolution)

    val unusedWords = mainInputWords.filterNot(refinedPuzzle.words.contains)
    resultInfoElement.innerHTML = HtmlRenderer.renderPuzzleInfo(refinedPuzzle, unusedWords)
    outputCluesElement.innerHTML = HtmlRenderer.renderClues(refinedPuzzle)


  def refineSolution(): Unit =
    val language = languageSelect.value
    val words = Globals.window(language)
    println(words.length)
    refinedPuzzle = Puzzle.finalize(initialPuzzle, words.toList)
    renderSolution()

  /** normalize words and expand german umlauts */
  private def normalizeWord(word: String): String =
    word.trim.toUpperCase.
      replace("Ä", "AE").
      replace("Ö", "OE").
      replace("Ü", "UE").
      replace("ß", "SS")
