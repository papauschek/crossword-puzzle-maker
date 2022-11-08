package com.papauschek.puzzle

import upickle.default.*

case class PuzzleConfig(width: Int = 18, height: Int = 18,
                        wrapping: Boolean = false
                       )

object PuzzleConfig {

  implicit val rw: ReadWriter[PuzzleConfig] = macroRW

}
