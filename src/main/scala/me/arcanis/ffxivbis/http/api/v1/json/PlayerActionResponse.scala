package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PlayerActionResponse(
  @Schema(description = "action to perform", required = true, `type` = "string", allowableValues = Array("add", "remove"), example = "add") action: ApiAction.Value,
  @Schema(description = "player description", required = true) playerIdResponse: PlayerResponse)
