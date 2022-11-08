package com.papauschek.puzzle

object Point {
  def apply(x: Int, y: Int) : Point = new Point((x.toShort & 0xffff) | (y.toShort << 16))
}

class Point(val underlying: Int) extends AnyVal {
  def x : Int = (underlying & 0xffff).toShort
  def y : Int = (underlying >>> 16).toShort
  override def toString: String = "Point(" + x + ", " + y + ")"
}

case class Rect(x: Int, y: Int, width: Int, height: Int)



