package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PieceActionResponse(
  @Schema(description = "action to perform", required = true, `type` = "string", allowableValues = Array("add", "remove")) action: ApiAction.Value,
  @Schema(description = "piece description", required = true) piece: PieceResponse,
  @Schema(description = "player description", required = true) playerIdResponse: PlayerIdResponse)
