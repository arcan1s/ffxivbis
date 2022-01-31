/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.helpers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import me.arcanis.ffxivbis.http.api.v1.json.ApiAction
import me.arcanis.ffxivbis.messages.DatabaseMessage._
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId, PlayerIdWithCounters}

import scala.concurrent.{ExecutionContext, Future}

trait LootHelper {

  def storage: ActorRef[Message]

  def addPieceLoot(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean)(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    storage.ask(AddPieceTo(playerId, piece, isFreeLoot, _))

  def doModifyLoot(action: ApiAction.Value, playerId: PlayerId, piece: Piece, maybeFree: Option[Boolean])(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    (action, maybeFree) match {
      case (ApiAction.add, Some(isFreeLoot)) => addPieceLoot(playerId, piece, isFreeLoot)
      case (ApiAction.remove, Some(isFreeLoot)) => removePieceLoot(playerId, piece, isFreeLoot)
      case _ => throw new IllegalArgumentException("Loot modification must always contain `isFreeLoot` field")
    }

  def loot(partyId: String, playerId: Option[PlayerId])(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Seq[Player]] =
    storage.ask(GetLoot(partyId, playerId, _))

  def removePieceLoot(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean)(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    storage.ask(RemovePieceFrom(playerId, piece, isFreeLoot, _))

  def suggestPiece(partyId: String, piece: Piece)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Seq[PlayerIdWithCounters]] =
    storage.ask(SuggestLoot(partyId, piece, _)).map(_.result)
}
