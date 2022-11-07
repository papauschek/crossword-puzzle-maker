package com.papauschek

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSGlobalScope}

@js.native
@JSGlobalScope
object WorkerGlobal extends js.Object {
  def addEventListener(`type`: String, f: js.Function): Unit = js.native
  def postMessage(data: js.Any): Unit = js.native
}

@JSExportTopLevel("WorkerMain")
object WorkerMain {

  @JSExport
  def main(): Unit = {
    WorkerGlobal.addEventListener("message", onMessage _ )
    WorkerGlobal.postMessage(s"Started")
  }

  def onMessage(msg: dom.MessageEvent) = {
    val s = msg.data.asInstanceOf[String]
    WorkerGlobal.postMessage(s"Received: $s")
  }

}