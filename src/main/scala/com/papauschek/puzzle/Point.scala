package com.papauschek.puzzle

/** a point with x and y coordinates.
 * - The coordinates can be up to 16 bit in size:
 * - for efficiency, this point uses an underlying 32 bit integer for both coordinates */
class Point(val underlying: Int) extends AnyVal:
  def x : Int = (underlying & 0xffff).toShort
  def y : Int = (underlying >>> 16).toShort
  override def toString: String = "Point(" + x + ", " + y + ")"


object Point:
  def apply(x: Int, y: Int) : Point = new Point((x.toShort & 0xffff) | (y.toShort << 16))


/** represents a character at a specific point in the puzzle,
 *  for which there is enough space in the puzzle that another
 *  word can be attached to it either vertically or horizontally. 
 * @param vertical if true, a vertically aligned word can be attached to this character.
 *                 otherwise, a horizontally aligned word can be attached. */
case class CharPoint(char: Char, x: Int, y: Int, vertical: Boolean)

/** the start of a word in the puzzle.
 * @param index the number of the word in the puzzle. the first word has index = 1.
 * @param vertical true if the word is oriented vertically in the puzzle, false if horizontally.
 * @param word the word that starts at the annotated position */
case class AnnotatedPoint(index: Int, vertical: Boolean, word: String)
