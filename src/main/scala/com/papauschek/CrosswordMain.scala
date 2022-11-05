package com.papauschek

import java.io.File
import java.nio.file.{Files, Path}
import scala.io.Source
import scala.util.Random

object CrosswordMain {

  def println(content: Any = ""): Unit = {
    System.out.println(content)
  }

  // generate
  /** density by word sorting strategy:
   - 55% sorted by frequency sum
   - 55% longest to shortest
   - 53% random
   - 53% sorted by frequency average
   - 52% shortest to longest
   */
  def create(mainWords: Seq[String], config: PuzzleConfig): Puzzle = {
    println("word db: " + mainWords.tail.length)
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
          System.out.println(s"Best rating: ${puzzle.rating} (${puzzle.density}%) - $count")
        }
        puzzles
    }.sortBy(- _.rating)
    val end = System.currentTimeMillis()
    println("ms: " + (end - start))

    puzzles.head
  }

}

