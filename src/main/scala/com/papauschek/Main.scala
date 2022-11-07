package com.papauschek

import org.scalajs.dom
import org.scalajs.dom.{Element, Worker}
import org.scalajs.dom.html.{Button, Div, Input, Select}
import scala.scalajs.js
import scala.scalajs.js.Object.{entries, keys}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main:

  @JSExport
  def main(): Unit = {
    println("Main started.")
    val w1 = new Worker("worker.js")
    val w2 = new Worker("worker.js")
    w1.onmessage = { msg => println(s"Received W1: ${msg.data}") }
    w2.onmessage = { msg => println(s"Received W2: ${msg.data}") }
    w1.postMessage("m1")
    w2.postMessage("m2")

    new MainPage
  }