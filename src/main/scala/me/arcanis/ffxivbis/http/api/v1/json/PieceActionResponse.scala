/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PieceActionResponse(
  @Schema(description = "action to perform", required = true, `type` = "string", allowableValues = Array("add", "remove")) action: ApiAction.Value,
  @Schema(description = "piece description", required = true) piece: PieceResponse,
  @Schema(description = "player description", required = true) playerIdResponse: PlayerIdResponse)
