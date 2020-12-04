/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.view

import scalatags.Text
import scalatags.Text.all._

object ErrorView {

  def template(error: Option[String]): Text.TypedTag[String] = error match {
    case Some(text) => p(id:="error", s"Error occurs: $text")
    case None => p("")
  }
}
