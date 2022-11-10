package com.papauschek.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSGlobalScope
object Globals extends js.Object:

  /** the Browser window */
  def window: Window = js.native


@js.native
trait Window extends js.Object:

  /** @return a word dictionary loaded in the Javascript window/global scope */
  @JSBracketAccess
  def apply(language: String): js.Array[String] = js.native
