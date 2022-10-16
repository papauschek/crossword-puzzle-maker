package com.papauschek


object HtmlRenderer {

  def render(puzzle: Puzzle): String = {

    val annotation = puzzle.getAnnotation

    def renderCell(x: Int, y: Int) : String = {
      puzzle.getChar(x, y) match {
        case ' ' => s"""<div class="cell cell-empty"></div>"""
        case char =>
          //s"""<div class="cell cell-char">$char</div>"""
          annotation.get(Point(x, y)) match {
            case Some(anno) =>
              val annotationIndices = anno.map(_.index).mkString(",")
              s"""<div class="cell cell-char cell-annotated"><span>$annotationIndices</span></div>"""
            case _ => s"""<div class="cell cell-char"></div>"""
          }
      }
    }

    def renderHeight(y: Int) : String = {
      val renderedRow =
        (0 until puzzle.config.width).map(renderCell(_, y)).mkString("\r\n")
      s"""<div class="row">$renderedRow</div>"""
    }

    val renderedPuzzle =
      (0 until puzzle.config.height).map(renderHeight).mkString("\r\n")

    val descriptions = annotation.values.flatten.toSeq.sortBy(a => (a.vertical, a.index)).map {
      p => "<div>" + p.index + ") " + p.word + "</div>"
    }.mkString("\r\n")

    s"""$renderedPuzzle
      |<div style="clear: both"></div>
      |<br><br><br><br><br>
      |$descriptions""".stripMargin
  }

}
