package com.papauschek.puzzle

import com.papauschek.*
import upickle.default.*
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

/** represents a complete crossword puzzle with all its characters and words.
 * @param chars represents the solution characters and empty spaces in the rectangular puzzle.
 *              a single array is used, and represents multiple lines of data, like a bitmap.
 *              the space character represents an empty field, other characters represent themselves
 * @param config the size of the puzzle
 * @param words contains all words that are part of the puzzle.
 *              note: words CAN contain spaces as well. */
case class Puzzle(chars: Array[Char],
                  config: PuzzleConfig,
                  words: Set[String]):

  /** calculate the density of this puzzle.
   * this is the sum of the length of all words in the puzzle, and
   * used to select the best puzzle out of many randomly generated ones. */
  val density: Double =
    var letters = 0
    words.foreach {
      word => letters += word.length
    }
    letters / chars.length.toDouble

  /** for each character available in the puzzle (= Char key), calculates possible locations (= Array[CharPoint])
   * where additional words could be attached to the puzzle */
  lazy val positions: Map[Char, Array[CharPoint]] =
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

  /** add the given word to the puzzle at the given location, and return a modified copy of the puzzle
   * @param vertical true if the word should be attached vertically (downward from the x/y location),
   *                 false if horizontally (to the right from the x/y location)
   * @param word the word to add to the puzzle */
  def copyWithWord(x: Int, y: Int, vertical: Boolean, word: String): Puzzle =
    val newChars = new Array[Char](chars.length)
    Array.copy(chars, 0, newChars, 0, chars.length)
    if (vertical) {
      (0 until word.length).foreach { index => newChars(toIndex(x, y + index)) = word(index) }
    } else {
      (0 until word.length).foreach { index => newChars(toIndex(x + index, y)) = word(index) }
    }
    copy(chars = newChars, words = words + word)

  /** @return the index of the given x/y puzzle location in the `chars` array */
  private def toIndex(x: Int, y: Int): Int = y * config.width + x % config.width

  /** @return the character in the puzzle at the given location.
   *          returns space character if the location is out of bounds of the puzzle */
  def getChar(x: Int, y: Int): Char =
    if (y < 0 || y >= config.height || ((x < 0 || x >= config.width) && !config.wrapping)) ' '
    else chars((x + config.width) % config.width + y * config.width) // handles x >= config.width for wrapping puzzle

  /** @return true if the given location in the puzzle is empty (= space character) */
  private def isEmpty(x: Int, y: Int): Boolean = getChar(x, y) == ' '

  /** @return true if the given location in the puzzle is filled with a character */
  private def hasChar(x: Int, y: Int): Boolean = getChar(x, y) != ' '

  /** tries to add the given word to the puzzle
   * @return array representing all possible variations of adding the word to the puzzle */
  def addWord(word: String): Array[Puzzle] =
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

  /** @return true if the given word, at the given x/y location, would fit into the puzzle
   * @param vertical if true, assumes the word should be added vertically, otherwise horizontally. */
  private def fits(word: String, vertical: Boolean, x: Int, y: Int) : Boolean =
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

  /** @return a simple text representation of the crossword puzzle, for debugging */
  override def toString: String =
    val scan = 0 until config.width
    val board = (0 until config.height).map {
      y => scan.map { x => getChar(x, y) }.mkString
    }.mkString(Seq.fill(config.width)('#').mkString + "##\r\n#", "#\r\n#", "#\r\n##" + Seq.fill(config.width)('#').mkString)
    s"Density: ${(density * 100).toInt}%, Words: ${words.size}, ${config.width}x${config.height}\r\n" + board

  /** @return all annotations for each point in the puzzle that needs to be annotated.
   *          an annotation represents the start of a word in the puzzle */
  def getAnnotation: Map[Point, Seq[AnnotatedPoint]] =
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
      val vWord = getWord(point, true)
      val hWord = getWord(point, false)
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

  /** @return reconstruct the word at the given point, assuming the given orientation */
  private def getWord(point: Point, vertical: Boolean): String =
    (for {
      c <- 0 until config.height.max(config.width)
    } yield {
      if (vertical) getChar(point.x, point.y + c)
      else getChar(point.x + c, point.y)
    }).takeWhile(!_.isWhitespace).mkString


  /** @return the set of locations in the puzzle that should be revealed in the partial solution in the UI
   * @param random the random generator used for selecting the revealed puzzle locations randomly
   * @param solvedFraction the fraction of the puzzle that should be revealed */
  def getCharsShownInPartialSolution(random: Random = new Random(hashCode),
                                     solvedFraction: Double = 0.30): Set[Point] =
    var resultSet = Set.empty[Point]
    for {
      x <- 0 until config.width
      y <- 0 until config.height if hasChar(x, y)
    } yield {
      val point = Point(x, y)
      // randomly select `solvedFraction` random fraction of points, but no direct neighbors
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


object Puzzle:

  /** macro for JSON serialization */
  implicit val rw: ReadWriter[Puzzle] = macroRW

  /** @return an empty puzzle of the given size */
  def empty(config: PuzzleConfig): Puzzle = Puzzle(Array.fill(config.width * config.height)(' '), config, Set.empty)

  /** @return randomly generated puzzles using the given word list and configuration
   * @param initialWord the word that should be used as the basis for the puzzle
   * @param list more words that should be added to the puzzle. not all words are guaranteed to be used, depending on size
   * @param config size of the puzzle */
  def generate(initialWord: String, list: List[String], config: PuzzleConfig): Seq[Puzzle] =
    val emptyPuzzle = empty(config)
    val puzzles = Seq(initial(emptyPuzzle, initialWord, false, false), initial(emptyPuzzle, initialWord, true, false))
    puzzles.map(p => generateAndFinalize(p, list))

  /** completes a puzzle by adding words to the given puzzle.
   * @param puzzle the existing puzzle that should be used as a basis. words will be added to this and a modified copy returned.
   * @param words more words that should be added to the puzzle. not all words are guaranteed to be used, depending on size */
  private def generateAndFinalize(puzzle: Puzzle, words: List[String]): Puzzle =
    var basePuzzle = puzzle
    var finalPuzzle = generate(basePuzzle, words, Nil)
    while (finalPuzzle.words.size > basePuzzle.words.size) {
      basePuzzle = finalPuzzle
      finalPuzzle = generate(basePuzzle, words, Nil)
    }
    finalPuzzle

  /** create a puzzle with only a single word in it, with the given orientation and alignment.
   * @param emptyPuzzle empty puzzle that is used as basis.
   * @param word the initial word that should be added
   * @param vertical true if the word should be added vertically, otherwise horizontally
   * @param center true if the word should be centered, otherwise it is placed randomly in the puzzle */
  private def initial(emptyPuzzle: Puzzle, word: String, vertical: Boolean, center: Boolean): Puzzle =
    if (!center) {
      if (vertical) emptyPuzzle.copyWithWord(Random.nextInt(emptyPuzzle.config.width), Random.nextInt(emptyPuzzle.config.height - word.length + 1), vertical, word)
      else emptyPuzzle.copyWithWord(Random.nextInt(emptyPuzzle.config.width - word.length + 1), Random.nextInt(emptyPuzzle.config.height), vertical, word)
    } else {
      if (vertical) emptyPuzzle.copyWithWord(emptyPuzzle.config.width / 2, (emptyPuzzle.config.height - word.length) / 2, vertical, word)
      else emptyPuzzle.copyWithWord((emptyPuzzle.config.width - word.length) / 2, emptyPuzzle.config.height / 2, vertical, word)
    }

  /** uses the given word list to try to fill remaining gaps in the puzzle
   * @param puzzle the puzzle for which we want to fill any gaps with additional words
   * @param words a long list (dictionary) of words that should be used to fill any gaps.
   *              for performance, not all words will be used if the list is very large */
  def finalize(puzzle: Puzzle, words: List[String]): Puzzle = {
    val sorted = Random.shuffle(words).take(10000).sortBy(- _.length)
    generate(puzzle, sorted, Nil)
  }

  /** uses the given word list to try to fill remaining gaps in the puzzle
   * @param puzzle the puzzle for which we want to fill any gaps with additional words
   * @param words a list of words that should be used to fill any gaps
   * @param tried list of words that were already tried, but could not be added to the puzzle */
  @tailrec private def generate(puzzle: Puzzle, words: List[String], tried: List[String]): Puzzle =
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

