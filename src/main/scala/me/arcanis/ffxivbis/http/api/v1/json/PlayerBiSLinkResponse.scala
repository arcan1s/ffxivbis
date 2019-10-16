package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PlayerBiSLinkResponse(
  @Schema(description = "link to player best in slot", required = true, example = "https://ffxiv.ariyala.com/19V5R") link: String,
  @Schema(description = "player description", required = true) playerId: PlayerIdResponse)
