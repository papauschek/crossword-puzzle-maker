package com.papauschek

import java.io.File
import java.nio.file.Files
import scala.io.Source
import scala.util.Random

object CrosswordMain {

  val MIN_LENGTH = 3
  val MAX_LENGTH = 8

  private lazy val ignored = Source.fromFile("ignored.txt", "UTF8").getLines().toList.map(_.trim).filter(_.nonEmpty).toSet

  private lazy val outputBuffer = new StringBuffer()

  def println(content: Any = ""): Unit = {
    System.out.println(content)
    outputBuffer.append(content.toString)
    outputBuffer.append("\r\n")
  }

  def wordFilter(words: List[String]) : List[String] = {
    words.map(_.toUpperCase.trim.
      replaceAllLiterally("Ä", "AE").
      replaceAllLiterally("Ö", "OE").
      replaceAllLiterally("Ü", "UE").
      replaceAllLiterally("ß", "SS")).
      filter(w => w.length >= 2 && !ignored(w))
  }

  def main: Unit = {

    val germanWords = wordFilter(Source.fromFile("german.txt", "UTF8").getLines().toList).filter(w => w.length >= MIN_LENGTH && w.length <= MAX_LENGTH)
    val englishWords = wordFilter(Source.fromFile("english.txt", "UTF8").getLines().toList).filter(w => w.length >= MIN_LENGTH && w.length <= MAX_LENGTH)
    val rawMainWords = wordFilter(Source.fromFile("input.txt", "UTF8").getLines().filter(!_.startsWith("#")).toList).distinct
    val mainWords = WordRating.getBest(rawMainWords)

    // generate
    var puzzle = loadOrCreate(mainWords.toList, "puzzle.json")
    //var puzzle = create(mainWords)
    //Puzzle.save(puzzle, "puzzle.json")

    println("density: " + (puzzle.density * 100).toInt)
    puzzle = Puzzle.finalize(puzzle, germanWords)
    println("density: " + (puzzle.density * 100).toInt)
    puzzle = Puzzle.finalize(puzzle, englishWords)
    println("density: " + (puzzle.density * 100).toInt)

    printFinalPuzzle(mainWords, puzzle)

    // render
    val output = HtmlRenderer.render(puzzle)
    Files.write(new File("puzzle.html").toPath, output.getBytes("UTF8"))
    Files.write(new File("output.txt").toPath, outputBuffer.toString.getBytes("UTF8"))
  }

  def loadOrCreate(mainWords: List[String], filename: String): Puzzle = {
    if (new File(filename).exists) {
      ??? //Puzzle.load(filename)
    } else {
      create(mainWords)
    }
  }

  // generate
  /** density by word sorting strategy:
   - 55% sorted by frequency sum
   - 55% longest to shortest
   - 53% random
   - 53% sorted by frequency average
   - 52% shortest to longest
   */
  def create(mainWords: Seq[String]): Puzzle = {
    println("word db: " + mainWords.tail.length)
    val start = System.currentTimeMillis()
    val config = PuzzleConfig(
      width = 12,
      height = 12
    )

    var bestRating = 0.0
    var count = 0
    val puzzles = (0 until 1000).flatMap {
      _ =>
        val puzzles = Puzzle.generate(mainWords.head, mainWords.tail.toList, config)
        val puzzle = puzzles.maxBy(_.rating)
        count += 1
        if (puzzle.rating > bestRating) {
          bestRating = puzzle.rating
          System.out.println(s"Best rating: ${puzzle.rating} (${puzzle.density}%) - $count")
        }
        puzzles
    }.seq.sortBy(- _.rating)
    val end = System.currentTimeMillis()
    println("ms: " + (end - start))

    puzzles.head
  }

  def printFinalPuzzle(mainWords: Seq[String], puzzle: Puzzle): Unit = {
    val (puzzleMain, puzzleOther) = puzzle.words.toSeq.sortBy(w => w).partition(mainWords.contains)
    println("not used:")
    println(mainWords.filterNot(puzzleMain.contains).mkString("\r\n"))
    println()
    println("main words used: " + puzzleMain.length + " / " + mainWords.length)
    println(puzzleMain.mkString("\r\n"))
    println()
    println(puzzleOther.mkString("\r\n"))
    println()
    println(puzzle)
  }

  def getWordsByRatings(words: Seq[String]) : Seq[(String, Double)] = {
    val chars = getCharFrequency(words)
    val ratings = words.map { word => (word, word.map(chars).sum) }
    ratings.sortBy(- _._2)
  }

  def getCharFrequency(words: Seq[String]) : Map[Char, Double] = {
    val counts = words.mkString.groupBy(c => c).map { case (c, list) => (c, list.length) }
    val sum = counts.values.sum.toDouble
    counts.map { case (c, count) => (c, (counts.size * count) / sum) }
  }

}

