package me.arcanis.ffxivbis.http.api.v1.json

import java.time.Instant

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.Loot

case class LootResponse(
  @Schema(description = "looted piece", required = true) piece: PieceResponse,
  @Schema(description = "loot timestamp", required = true) timestamp: Instant = Instant.now) {
  def toLoot: Loot = Loot(-1, piece.toPiece, timestamp)
}

object LootResponse {
  def fromLoot(loot: Loot): LootResponse =
    LootResponse(PieceResponse.fromPiece(loot.piece), loot.timestamp)
}