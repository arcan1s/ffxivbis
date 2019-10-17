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
import me.arcanis.ffxivbis.models.{BiS, Job, Player}

case class PlayerResponse(
  @Schema(description = "unique party ID", required = true, example = "abcdefgh") partyId: String,
  @Schema(description = "job name", required = true, example = "DNC") job: String,
  @Schema(description = "player nick name", required = true, example = "Siuan Sanche") nick: String,
  @Schema(description = "pieces in best in slot") bis: Option[Seq[PieceResponse]],
  @Schema(description = "looted pieces") loot: Option[Seq[PieceResponse]],
  @Schema(description = "link to best in slot", example = "https://ffxiv.ariyala.com/19V5R") link: Option[String],
  @Schema(description = "player loot priority") priority: Option[Int]) {
  def toPlayer: Player =
    Player(partyId, Job.fromString(job), nick,
      BiS(bis.getOrElse(Seq.empty).map(_.toPiece)), loot.getOrElse(Seq.empty).map(_.toPiece),
      link, priority.getOrElse(0))
}

object PlayerResponse {
  def fromPlayer(player: Player): PlayerResponse =
    PlayerResponse(player.partyId, player.job.toString, player.nick,
      Some(player.bis.pieces.map(PieceResponse.fromPiece)), Some(player.loot.map(PieceResponse.fromPiece)),
      player.link, Some(player.priority))
}
