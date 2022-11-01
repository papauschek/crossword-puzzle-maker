package com.papauschek

import org.scalajs.dom

object HtmlRenderer:

  def renderPuzzle(puzzle: Puzzle, widthInPixels: Int,
                   showSolution: Boolean = false,
                   showPartialSolution: Boolean = false): String =

    val annotation = puzzle.getAnnotation

    val partialPoints = puzzle.getCharsShownInPartialSolution()

    def renderCell(x: Int, y: Int): String =
      puzzle.getChar(x, y) match {
        case ' ' => s"""<div class="crossword-cell crossword-cell-empty"></div>"""
        case char =>
          val shownSolution = {
            if (showSolution ||
              (showPartialSolution && partialPoints.contains(Point(x, y)))) {
              s"""<span class="solution">$char</span>"""
            } else {
              ""
            }
          }
          annotation.get(Point(x, y)) match {
            case Some(anno) if anno.nonEmpty =>
              val annotationIndices = anno.map(_.index).mkString(",")
              s"""<div class="crossword-cell crossword-cell-char crossword-cell-annotated">$shownSolution<span class="annotation">$annotationIndices</span></div>""".stripMargin
            case _ => s"""<div class="crossword-cell crossword-cell-char">$shownSolution</div>"""
          }
      }

    def renderHeight(y: Int): String =
      val cellWidth = widthInPixels / puzzle.config.width
      val style = s"font-size: ${cellWidth}px"
      val renderedRow = (0 until puzzle.config.width).map(renderCell(_, y)).mkString("\r\n")
      s"""<div class="crossword-row" style="$style">$renderedRow</div>"""

    val renderedPuzzle =
      (0 until puzzle.config.height).map(renderHeight).mkString("\r\n")

    val sortedAnnotationValues = annotation.values.flatten.toSeq.sortBy(_.index)

    def renderDescriptions(vertical: Boolean): String = {
      sortedAnnotationValues.filter(_.vertical == vertical).map {
        p => "<div>" + p.index + ") " + p.word + "</div>"
      }.mkString("\r\n")
    }

    s"""$renderedPuzzle
      |<div style="clear: both" class="mb-5"></div>
      |<div class="row">
      |  <div class="col-lg-6">
      |    <h4>Horizontal</h4>
      |    <p>${renderDescriptions(vertical = false)}</p>
      |  </div>
      |  <div class="col-lg-6">
      |    <h4>Vertical</h4>
      |    <p>${renderDescriptions(vertical = true)}</p>
      |  </div>
      |</div>
      |""".stripMargin


  def renderPuzzleInfo(puzzle: Puzzle, unusedWords: Seq[String]): String =
    val infoText = s"This puzzle has a <strong>density of ${(puzzle.density * 100).round}%</strong>. " +
      s"This is the area covered by letters. " +
      s"If you prefer a more dense puzzle, add more words to the list above and let the tool discard the words that do not fit well. "
    val unusedInfoText = Option.when(unusedWords.nonEmpty)(s"The following words from your list were NOT used: ${unusedWords.mkString(", ")}").mkString
    infoText + unusedInfoText

