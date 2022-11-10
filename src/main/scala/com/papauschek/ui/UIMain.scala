package com.papauschek.ui

import com.papauschek.ui.MainPage
import org.scalajs.dom
import org.scalajs.dom.html.{Button, Div, Input, Select}
import org.scalajs.dom.{Element, Worker}
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Object.{entries, keys}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import concurrent.ExecutionContext.Implicits.global

@JSExportTopLevel("UIMain")
object UIMain:

  /** run the UI application */
  @JSExport
  def main(): Unit = {
    new MainPage
  }