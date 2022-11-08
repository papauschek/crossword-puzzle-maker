package com.papauschek.puzzle

import com.papauschek.*
import upickle.default.*

import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

case class CharPoint(char: Char, x: Int, y: Int, vertical: Boolean)

case class Puzzle(chars: Array[Char], // fields of chars, empty = space char.
                  config: PuzzleConfig, // size
                  words: Set[String]) { // contained words

  val (rating: Double, charCount: Int, density: Double) = {

    var index = 0
    var charCount = 0
    chars.foreach {
      char =>
        if (char != ' ') {
          charCount += 1
        }
        index += 1
    }

    var letters = 0
    words.foreach {
      word => letters += word.length
    }

    val rating = letters / index.toDouble
    val density = charCount / index.toDouble
    (rating, charCount, density)
  }

  lazy val positions : Map[Char, Array[CharPoint]] = {
    val list = ListBuffer.empty[CharPoint]
    (0 until chars.length).foreach {
      index =>
        val char = chars(index)
        if (char != ' ') {
          val x = index % config.width
          val y = index / config.width
          if (isEmpty(x + 1, y) && isEmpty(x - 1, y) &&
                ((isEmpty(x + 1, y + 1) && isEmpty(x + 1, y - 1) && x < config.width - 1)
              || (isEmpty(x - 1, y + 1) && isEmpty(x - 1, y - 1) && x > 0))) {
            list += CharPoint(char, x, y, false) // horizontal
          } else if (isEmpty(x, y + 1) && isEmpty(x, y - 1) &&
                ((isEmpty(x + 1, y + 1) && isEmpty(x - 1, y + 1) && y < config.height - 1)
              || (isEmpty(x + 1, y - 1) && isEmpty(x - 1, y - 1) && y > 0))) {
            list += CharPoint(char, x, y, true) // vertical
          }
        }
    }
    list.toArray.groupBy(_.char)
  }

  def copyWithWord(x: Int, y: Int, vertical: Boolean, word: String): Puzzle = {
    val newChars = new Array[Char](chars.length)
    Array.copy(chars, 0, newChars, 0, chars.length)
    if (vertical) {
      (0 until word.length).foreach { index => newChars(toIndex(x, y + index)) = word(index) }
    } else {
      (0 until word.length).foreach { index => newChars(toIndex(x + index, y)) = word(index) }
    }
    copy(chars = newChars, words = words + word)
  }

  private def toIndex(x: Int, y: Int): Int = y * config.width + x % config.width

  def getChar(x: Int, y: Int): Char = {
    if (y < 0 || y >= config.height || ((x < 0 || x >= config.width) && !config.wrapping)) ' '
    else chars((x + config.width) % config.width + y * config.width) // handles x >= config.width for wrapping puzzle
  }

  private def isEmpty(x: Int, y: Int): Boolean = getChar(x, y) == ' '

  private def hasChar(x: Int, y: Int): Boolean = getChar(x, y) != ' '

  def addWord(word: String): Array[Puzzle] = {

    val puzzles = for {
      (char, indicesList) <- word.zipWithIndex.groupBy(_._1).toList
      point <- positions.getOrElse(char, Array.empty[CharPoint])
      (_, index) <- indicesList
      (x, y, r, b) =
        if (point.vertical) (point.x, point.y - index, point.x, point.y - index + word.length - 1)
        else (point.x - index, point.y, point.x - index + word.length - 1, point.y)
      if x >= 0 && y >= 0 && b < config.height &&
        (r < config.width || (config.wrapping && x < config.width)) &&
        fits(word, point.vertical, x, y)
    } yield {
      copyWithWord(x, y, point.vertical, word)
    }

    puzzles.toArray
  }

  private def fits(word: String, vertical: Boolean, x: Int, y: Int) : Boolean = {
    var connect = false
    (0 until word.length).forall {
      index =>
        val (locX, locY) = if (vertical) (x, y + index) else (x + index, y)
        val existingChar = getChar(locX, locY)

        val same = existingChar == word(index)
        val isEmpty = existingChar == ' '
        if (same) connect = true

        (same || isEmpty) && // field is empty or char already there
          (!isEmpty || {
            // check adjacent fields for added chars
            if (vertical) (!hasChar(locX - 1, locY) && !hasChar(locX + 1, locY))
            else (!hasChar(locX, locY - 1) && !hasChar(locX, locY + 1))
          })
    } && connect && {
      // test ends of word
      if (vertical) (!hasChar(x, y - 1) && !hasChar(x, y + word.length))
      else (!hasChar(x - 1, y) && !hasChar(x + word.length, y))
    }
  }

  override def toString: String = {
    val scan = (0 until config.width)
    val board = (0 until config.height).map {
      y => scan.map { x => getChar(x, y) }.mkString
    }.mkString(Seq.fill(config.width)('#').mkString + "##\r\n#", "#\r\n#", "#\r\n##" + Seq.fill(config.width)('#').mkString)
    s"Density: ${(density * 100).toInt}%, ${(rating * 100).toInt}, Words: ${words.size}, ${config.width}x${config.height}\r\n" +
      board
  }

  def getAnnotation: Map[Point, Seq[AnnotatedPoint]] = {
    var index = 0
    val points = for {
      x <- 0 until config.width
      y <- 0 until config.height
      nonEmpty = !isEmpty(x, y)
      vertical = isEmpty(x, y - 1)
      horizontal = isEmpty(x - 1, y)
        if nonEmpty && (vertical || horizontal)
    } yield {
      val point = Point(x, y)
      val vWord = findWord(point, true)
      val hWord = findWord(point, false)
      val vert = if (vertical && vWord.length > 1) {
        index += 1
        Seq(AnnotatedPoint(index, true, vWord))
      } else Nil
      val horiz = if (horizontal && hWord.length > 1) {
        index += 1
        Seq(AnnotatedPoint(index, false, hWord))
      } else Nil
      (point, vert ++ horiz)
    }
    points.toMap
  }

  private def findWord(point: Point, vertical: Boolean) : String = {
    (for {
      c <- 0 until config.height.max(config.width)
    } yield {
      if (vertical) getChar(point.x, point.y + c)
      else getChar(point.x + c, point.y)
    }).takeWhile(!_.isWhitespace).mkString
  }

  def getCharsShownInPartialSolution(random: Random = new Random(hashCode),
                                     solvedFraction: Double = 0.30): Set[Point] = {
    var resultSet = Set.empty[Point]

    // randomly select `solvedFraction` random fraction of points, but no direct neighbors
    for {
      x <- 0 until config.width
      y <- 0 until config.height if hasChar(x, y)
    } yield {
      val point = Point(x, y)
      val hasVerticalNeighbor = hasChar(x, y - 1) || hasChar(x, y + 1)
      val hasHorizontalNeighbor = hasChar(x - 1, y) || hasChar(x + 1, y)
      val isIntersection = hasVerticalNeighbor && hasHorizontalNeighbor
      if (!isIntersection
        && random.nextDouble() < solvedFraction
        && !resultSet.contains(Point(point.x - 1, point.y))
        && !resultSet.contains(Point(point.x, point.y - 1))) {
        resultSet += point
      }
    }

    resultSet
  }

}

case class AnnotatedPoint(index: Int, vertical: Boolean, word: String)

object Puzzle {

  implicit val rw: ReadWriter[Puzzle] = macroRW

  def empty(config: PuzzleConfig) : Puzzle = Puzzle(Array.fill(config.width * config.height)(' '), config, Set.empty)

  def generate(initialWord: String, list: List[String], config: PuzzleConfig) : Seq[Puzzle] = {
    val emptyPuzzle = empty(config)
    val puzzles = Seq(initial(emptyPuzzle, initialWord, false, false), initial(emptyPuzzle, initialWord, true, false))
    puzzles.map(p => generateAndFinalize(p, list))
  }

  private def generateAndFinalize(puzzle: Puzzle, words: List[String]): Puzzle = {
    var basePuzzle = puzzle
    var finalPuzzle = generate(basePuzzle, words, Nil)
    while (finalPuzzle.words.size > basePuzzle.words.size) {
      basePuzzle = finalPuzzle
      finalPuzzle = generate(basePuzzle, words, Nil)
    }
    finalPuzzle
  }

  private def initial(emptyPuzzle: Puzzle, word: String, vertical: Boolean, center: Boolean) : Puzzle = {
    if (!center) {
      if (vertical) emptyPuzzle.copyWithWord(Random.nextInt(emptyPuzzle.config.width), Random.nextInt(emptyPuzzle.config.height - word.length + 1), vertical, word)
      else emptyPuzzle.copyWithWord(Random.nextInt(emptyPuzzle.config.width - word.length + 1), Random.nextInt(emptyPuzzle.config.height), vertical, word)
    } else {
      if (vertical) emptyPuzzle.copyWithWord(emptyPuzzle.config.width / 2, (emptyPuzzle.config.height - word.length) / 2, vertical, word)
      else emptyPuzzle.copyWithWord((emptyPuzzle.config.width - word.length) / 2, emptyPuzzle.config.height / 2, vertical, word)
    }
  }

  def finalize(puzzle: Puzzle, words: List[String]) : Puzzle = {
    val sorted = Random.shuffle(words).take(10000).sortBy(- _.length)
    generate(puzzle, sorted, Nil)
  }

  @tailrec private def generate(puzzle: Puzzle, words: List[String], tried: List[String]) : Puzzle = {
    if (words.isEmpty) {
      puzzle
    } else {

      if (puzzle.words.contains(words.head)) {
        generate(puzzle, words.tail, tried)
      } else {
        val options = puzzle.addWord(words.head)
        if (options.isEmpty) {
          generate(puzzle, words.tail, words.head +: tried)
        } else {
          val nextPuzzle = options(Random.nextInt(options.length))
          generate(nextPuzzle, tried ++ words.tail, Nil)
        }
      }

    }
  }


  /*
  implicit val format1 = Json.format[PuzzleConfig]

  implicit val format2 = new Format[Puzzle] {
    override def reads(json: JsValue): JsResult[Puzzle] = {
      JsSuccess(Puzzle(
        (json \ "chars").as[String].toCharArray,
        (json \ "config").as[PuzzleConfig],
        (json \ "words").as[Set[String]]
      ))
    }

    override def writes(o: Puzzle): JsValue = Json.obj(
      "chars" -> o.chars.mkString,
      "config" -> o.config,
      "words" -> o.words
    )
  }

  def save(puzzle: Puzzle, filename: String): Unit = {
    Files.write(Paths.get(filename), Json.toJson(puzzle).toString().getBytes("UTF-8"))
  }

  def load(filename: String): Puzzle = {
    val json = Json.parse(Source.fromFile(filename, "UTF-8").mkString)
    json.as[Puzzle]
  }
  */


}
