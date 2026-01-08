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
import me.arcanis.ffxivbis.models.{BiS, Job, Player}

case class PlayerModel(
  @Schema(description = "unique party ID", required = true, example = "o3KicHQPW5b0JcOm5yI3") partyId: String,
  @Schema(description = "job name", required = true, example = "DNC") job: String,
  @Schema(description = "player nick name", required = true, example = "Siuan Sanche") nick: String,
  @Schema(description = "pieces in best in slot") bis: Option[Seq[PieceModel]],
  @Schema(description = "looted pieces") loot: Option[Seq[LootModel]],
  @Schema(description = "link to best in slot", example = "https://ffxiv.ariyala.com/19V5R") link: Option[String],
  @Schema(description = "player loot priority", `type` = "number") priority: Option[Int],
  @Schema(
    description = "count of looted pieces which are parts of best in slot",
    `type` = "number"
  ) lootCountBiS: Option[Int],
  @Schema(description = "total count of looted pieces", `type` = "number") lootCountTotal: Option[Int],
) extends Validator {

  require(isValidString(nick), stringMatchError("Player name"))
  require(link.forall(isValidString), stringMatchError("BiS link"))

  def toPlayer: Player =
    Player(
      -1,
      partyId,
      Job.withName(job),
      nick,
      BiS(bis.getOrElse(Seq.empty).map(_.toPiece)),
      loot.getOrElse(Seq.empty).map(_.toLoot),
      link,
      priority.getOrElse(0)
    )
}

object PlayerModel {

  def fromPlayer(player: Player): PlayerModel =
    PlayerModel(
      player.partyId,
      player.job.toString,
      player.nick,
      Some(player.bis.pieces.map(PieceModel.fromPiece)),
      Some(player.loot.map(LootModel.fromLoot)),
      player.link,
      Some(player.priority),
      Some(player.lootCountBiS),
      Some(player.lootCountTotal),
    )
}
