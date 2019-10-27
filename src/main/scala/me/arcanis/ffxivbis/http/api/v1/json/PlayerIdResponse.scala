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
import me.arcanis.ffxivbis.models.{Job, PlayerId}

case class PlayerIdResponse(
  @Schema(description = "unique party ID. Required in responses", example = "abcdefgh") partyId: Option[String],
  @Schema(description = "job name", required = true, example = "DNC") job: String,
  @Schema(description = "player nick name", required = true, example = "Siuan Sanche") nick: String) {
  def withPartyId(partyId: String): PlayerId =
    PlayerId(partyId, Job.withName(job), nick)
}

object PlayerIdResponse {
  def fromPlayerId(playerId: PlayerId): PlayerIdResponse =
    PlayerIdResponse(Some(playerId.partyId), playerId.job.toString, playerId.nick)
}
