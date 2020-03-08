/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.http.api.v1.json.ApiAction
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId, PlayerIdWithCounters}
import me.arcanis.ffxivbis.service.LootSelector.LootSelectorResult
import me.arcanis.ffxivbis.service.impl.DatabaseLootHandler

import scala.concurrent.{ExecutionContext, Future}

trait LootHelper {

  def storage: ActorRef

  def addPieceLoot(playerId: PlayerId, piece: Piece)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseLootHandler.AddPieceTo(playerId, piece)).mapTo[Int]

  def doModifyLoot(action: ApiAction.Value, playerId: PlayerId, piece: Piece)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    action match {
      case ApiAction.add => addPieceLoot(playerId, piece)
      case ApiAction.remove => removePieceLoot(playerId, piece)
    }

  def loot(partyId: String, playerId: Option[PlayerId])
          (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    (storage ? DatabaseLootHandler.GetLoot(partyId, playerId)).mapTo[Seq[Player]]

  def removePieceLoot(playerId: PlayerId, piece: Piece)
                     (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseLootHandler.RemovePieceFrom(playerId, piece)).mapTo[Int]

  def suggestPiece(partyId: String, piece: Piece)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[PlayerIdWithCounters]] =
    (storage ? DatabaseLootHandler.SuggestLoot(partyId, piece)).mapTo[LootSelectorResult].map(_.result)
}
