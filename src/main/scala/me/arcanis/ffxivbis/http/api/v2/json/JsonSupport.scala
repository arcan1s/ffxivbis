/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v2.json

import me.arcanis.ffxivbis.http.api.v1.json.{JsonSupport => JsonSupportV1}
import spray.json._

trait JsonSupport extends JsonSupportV1 {

  implicit val piecesFormat: RootJsonFormat[PiecesModel] = jsonFormat1(PiecesModel.apply)
}
