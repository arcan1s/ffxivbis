package me.arcanis.ffxivbis.http.view

import scalatags.Text
import scalatags.Text.all._

object ErrorView {
  def template(error: Option[String]): Text.TypedTag[String] = error match {
    case Some(text) => p(id:="error", s"Error occurs: $text")
    case None => p("")
  }
}
