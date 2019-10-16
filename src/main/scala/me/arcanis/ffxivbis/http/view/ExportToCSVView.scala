package me.arcanis.ffxivbis.http.view

import scalatags.Text
import scalatags.Text.all._

object ExportToCSVView {
  def template: Text.TypedTag[String] =
    div(
      button(onclick:="exportTableToCsv('result.csv')")("Export to CSV"),
      script(src:="/static/table_export.js", `type`:="text/javascript")
    )
}
