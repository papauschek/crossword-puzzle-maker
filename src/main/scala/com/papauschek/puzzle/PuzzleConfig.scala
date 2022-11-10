package com.papauschek.puzzle

import upickle.default.*

/** size and topology of the puzzle
 * @param width the number of horizontal characters in the puzzle
 * @param height the number of vertical characters in the puzzle
 * @param wrapping true if the puzzle should wrap around horizontally.
                   can be used for puzzles that wrap around e.g. a cylinder horizontally. */
case class PuzzleConfig(width: Int = 18, height: Int = 18,
                        wrapping: Boolean = false
                       )

object PuzzleConfig {

  /** macro for JSON serialization */
  implicit val rw: ReadWriter[PuzzleConfig] = macroRW

}
