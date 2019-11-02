package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class PartyIdResponse(
  @Schema(description = "party id", required = true) partyId: String)
