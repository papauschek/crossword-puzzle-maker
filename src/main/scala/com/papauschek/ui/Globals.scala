package com.papauschek.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSGlobalScope
object Globals extends js.Object {

  def window: Window = js.native

}

@js.native
trait Window extends js.Object {

  @JSBracketAccess
  def apply(language: String): js.Array[String] = js.native

}