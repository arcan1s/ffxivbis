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
import me.arcanis.ffxivbis.models.PlayerIdWithCounters

case class PlayerIdWithCountersResponse(
  @Schema(description = "unique party ID", required = true, example = "abcdefgh") partyId: String,
  @Schema(description = "job name", required = true, example = "DNC") job: String,
  @Schema(description = "player nick name", required = true, example = "Siuan Sanche") nick: String,
  @Schema(description = "is piece required by player or not", required = true) isRequired: Boolean,
  @Schema(description = "player loot priority", required = true) priority: Int,
  @Schema(description = "count of savage pieces in best in slot", required = true) bisCountTotal: Int,
  @Schema(description = "count of looted pieces", required = true) lootCount: Int,
  @Schema(description = "count of looted pieces which are parts of best in slot", required = true) lootCountBiS: Int,
  @Schema(description = "total count of looted pieces", required = true) lootCountTotal: Int
)

object PlayerIdWithCountersResponse {

  def fromPlayerId(playerIdWithCounters: PlayerIdWithCounters): PlayerIdWithCountersResponse =
    PlayerIdWithCountersResponse(
      playerIdWithCounters.partyId,
      playerIdWithCounters.job.toString,
      playerIdWithCounters.nick,
      playerIdWithCounters.isRequired,
      playerIdWithCounters.priority,
      playerIdWithCounters.bisCountTotal,
      playerIdWithCounters.lootCount,
      playerIdWithCounters.lootCountBiS,
      playerIdWithCounters.lootCountTotal
    )
}
