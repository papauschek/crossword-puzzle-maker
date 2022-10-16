package com.papauschek

object DomTests {

}

/**
package alchemist

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Button, Div}

object View {

  def createDiv(className: String): Div = {
    val element = dom.document.createElement("div").asInstanceOf[Div]
    element.classList.add(className)
    element
  }

  def createButton(text: String): Button = {
    val element = dom.document.createElement("button").asInstanceOf[Button]
    element.textContent = text
    element.classList.add("game-button")
    element
  }

  def setVisible(element: Element, visible: Boolean): Unit ={
    if (visible) {
      element.removeAttribute("style")
    } else {
      element.setAttribute("style", "display: none;")
    }
  }

}



import org.scalajs.dom
import org.scalajs.dom.Element
import scala.util.Random

class MainViewController(var state: UIState) {

  val wrapper: Element = View.createDiv("wrapper")
  val menuView: MenuView = new MenuView()
  val fieldContainer: Element = View.createDiv("field-container")
  val fieldRatio: Element = View.createDiv("field-ratio")

  // all field views
  val fieldViews: List[FieldView] = Field.allFields.map {
    field =>
      val fieldView = new FieldView(field)
      fieldRatio.appendChild(fieldView.element)
      fieldView.marbleElement.onclick = { _ => clickMarble(field) }
      fieldView
  }

  init()

  private def init(): Unit = {

    // root element
    dom.document.body.appendChild(wrapper)

    // menu
    wrapper.appendChild(menuView.element)
    menuView.restartButton.onclick = { _ => clickRestart() }
    menuView.newButton.onclick = { _ => clickNewGame() }
    menuView.resetButton.onclick = { _ => clickReset() }
    menuView.levelButton.onclick = { _ => clickSkip() }

    // stats
    menuView.statsViews.foreach {
      v =>
        v.marbleElement.onmouseover = { _ => clickStatsMarble(Some(v.marble)) }
        v.marbleElement.onclick = { _ => clickStatsMarble(Some(v.marble)) }
    }

    // game field
    fieldContainer.appendChild(fieldRatio)
    wrapper.appendChild(fieldContainer)

    // resize
    dom.window.onresize = { _ => onresize() }
    onresize()

    animateDelayed()
  }

  private def onresize(): Unit = {
    val minHeightPercentage = 0.64
    val maxWidth = (dom.window.innerHeight / minHeightPercentage).toInt
    val width = dom.window.innerWidth.min(maxWidth)
    val fontSize = width / 100
    wrapper.setAttribute("style", s"max-width: ${maxWidth}px; font-size: ${fontSize}px")
  }

  private def clickMarble(point: Point): Unit = {
    state = state.selectField(point).copy(highlightedMarble = None)
    update()

    if (state.game.field.isWon) {
      setFieldWon(won = true)
    }
  }

  private def clickStatsMarble(marble: Option[Marble]): Unit = {
    state = state.copy(highlightedMarble = marble, selectedField = None)
    update()
  }

  private def clickRestart(): Unit = {
    animateDelayed {
      state = state.copy(game = GameCreator.createGame(state.level, state.seed))
      startGame()
    }
  }

  private def clickNewGame(): Unit = {
    animateDelayed {
      val level: Int = if (state.game.field.isWon) state.level + 1 else state.level
      val seed: Int = if (state.game.field.isWon) state.seed else Random.nextInt()
      val score: Int = if (state.game.field.isWon) state.score + 1 else state.score
      state = state.copy(game = GameCreator.createGame(level, seed), level = level, seed = seed, score = score)
      startGame()
    }
  }

  private def clickReset(): Unit = {
    if (dom.window.confirm("Are you sure?")) {
      animateDelayed {
        val level = 1
        state = state.copy(game = GameCreator.createGame(level, state.seed), level = level)
        startGame()
      }
    }
  }

  private def clickSkip(): Unit = {
    if (dom.window.confirm("Are you sure?")) {
      animateDelayed {
        val level = state.level + 1
        state = state.copy(game = GameCreator.createGame(level, state.seed), level = level)
        startGame()
      }
    }
  }

  private def animateDelayed(block: => Unit): Unit = {
    setFieldWon(won = false)
    resetFieldTransition(reset = true)
    setFieldLoading(loading = true)
    delayed {
      resetFieldTransition(reset = false)
      setFieldLoading(loading = false)
      block
    }
  }

  private def delayed(block: => Unit): Unit = {
    dom.window.setTimeout(() => block,1)
  }

  /** animate new game */
  private def startGame(): Unit = {
    state = state.copy(selectedField = None)
    GameStorage.setScore(state.score)
    GameStorage.setLevel(state.level)
    update()
  }

  private def resetFieldTransition(reset: Boolean): Unit = {
    fieldContainer.classList.toggle("no-transition", force = reset)
  }

  private def setFieldLoading(loading: Boolean): Unit = {
    fieldContainer.classList.toggle("field-loading", force = loading)
  }

  private def setFieldWon(won: Boolean): Unit = {
    fieldContainer.classList.toggle("field-won", force = won)
  }

  def update(): Unit = {

    // win state
    dom.document.body.classList.toggle("game-lost", !state.game.canWin)

    // menu
    menuView.update(state)

    fieldViews.foreach {
      fieldView =>
        val marble = state.game.field.marbles.get(fieldView.point)
        fieldView.setContent(marble,
          isSelected = state.selectedField.contains(fieldView.point),
          isSelectable = state.game.field.selectableMarbles.contains(fieldView.point),
          isHighlighted = marble.exists(state.highlightedMarble.contains)
          )
    }
  }

}
*/