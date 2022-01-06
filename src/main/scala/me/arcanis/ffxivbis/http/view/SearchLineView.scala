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

object SearchLineView {

  def template: Text.TypedTag[String] =
    div(
      input(
        `type` := "text",
        id := "search",
        onkeyup := "searchTable()",
        placeholder := "search for data",
        title := "search"
      )
    )
}
