/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.Loot

import java.time.Instant

case class LootModel(
  @Schema(description = "looted piece", required = true) piece: PieceModel,
  @Schema(description = "loot timestamp", required = true) timestamp: Instant,
  @Schema(description = "is loot free for all", required = true) isFreeLoot: Boolean
) {

  def toLoot: Loot = Loot(-1, piece.toPiece, timestamp, isFreeLoot)
}

object LootModel {

  def fromLoot(loot: Loot): LootModel =
    LootModel(PieceModel.fromPiece(loot.piece), loot.timestamp, loot.isFreeLoot)
}
