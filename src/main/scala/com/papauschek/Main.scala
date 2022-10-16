package com.papauschek

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Button, Div}

object Main:

  def main(args: Array[String]): Unit =
    println("Hello world!")
    val inputWords = dom.document.getElementById("input").innerHTML.linesIterator.map(_.trim.toUpperCase).filter(_.nonEmpty).toSeq
    println()
    println("test")
    val mainWords = WordRating.getBest(inputWords)
    val puzzle = CrosswordMain.create(mainWords)
    val html = HtmlRenderer.render(puzzle)
    dom.document.getElementById("output1").innerHTML = html
