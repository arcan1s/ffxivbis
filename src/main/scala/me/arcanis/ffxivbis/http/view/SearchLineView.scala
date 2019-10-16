package me.arcanis.ffxivbis.http.view

import scalatags.Text
import scalatags.Text.all._

object SearchLineView {
  def template: Text.TypedTag[String] =
    div(
      input(
        `type`:="text", id:="search", onkeyup:="searchTable()",
        placeholder:="search for data", title:="search"
      )
    )
}
