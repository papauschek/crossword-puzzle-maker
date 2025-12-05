package com.papauschek.puzzle

import org.scalatest.*
import org.scalatest.funsuite.AnyFunSuite

class PuzzleSuite extends AnyFunSuite:

  test("Connected word annotation should work") {
    val testWords = Seq("test multi", "tee")
    val puzzles = Puzzle.generate(testWords.head, testWords.tail.toList, PuzzleConfig(10, 10))
    val puzzle = puzzles.head
    println(puzzle)
    val annotatedWords = puzzle.getAnnotation.values.flatten.map(_.word).toList.sorted
    assert(annotatedWords == List("multi", "tee", "test"))
  }

  test("Full annotation should reconstruct words with spaces") {
    val testWords = Seq("test multi", "tee")
    val puzzles = Puzzle.generate(testWords.head, testWords.tail.toList, PuzzleConfig(10, 10))
    val puzzle = puzzles.head
    println(puzzle)

    val fullAnnotatedWords = puzzle.getFullAnnotation
    val fullWords = fullAnnotatedWords.map(_.fullWord).sorted

    // Should have all original words including the one with space
    assert(fullWords.contains("test multi"))
    assert(fullWords.contains("tee"))

    // Verify each AnnotatedWord has proper structure
    fullAnnotatedWords.foreach { aw =>
      assert(aw.index > 0, "Index should be positive")
      assert(aw.fullWord.nonEmpty, "Full word should not be empty")

      // Verify the word actually exists at the specified location
      val word = aw.fullWord
      val startX = aw.location.x
      val startY = aw.location.y
      word.zipWithIndex.foreach { case (char, i) =>
        val (x, y) = if (aw.vertical) (startX, startY + i) else (startX + i, startY)
        assert(puzzle.getChar(x, y) == char,
          s"Character mismatch for word '${word}' at position ${i}: expected '${char}', got '${puzzle.getChar(x, y)}'")
      }
    }

    println(s"Full annotations: ${fullAnnotatedWords.map(aw => s"${aw.index}. ${aw.fullWord} (${if (aw.vertical) "vertical" else "horizontal"})").mkString(", ")}")
  }


