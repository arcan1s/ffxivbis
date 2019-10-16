package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId, PlayerIdWithCounters}
import me.arcanis.ffxivbis.service.LootSelector.LootSelectorResult
import me.arcanis.ffxivbis.service.impl.DatabaseLootHandler

import scala.concurrent.{ExecutionContext, Future}

class LootHelper(storage: ActorRef) {

  def addPieceLoot(playerId: PlayerId, piece: Piece)
                  (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseLootHandler.AddPieceTo(playerId, piece) }

  def loot(partyId: String, playerId: Option[PlayerId])
          (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    (storage ? DatabaseLootHandler.GetLoot(partyId, playerId)).mapTo[Seq[Player]]

  def removePieceLoot(playerId: PlayerId, piece: Piece)
                     (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseLootHandler.RemovePieceFrom(playerId, piece) }

  def suggestPiece(partyId: String, piece: Piece)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[PlayerIdWithCounters]] =
    (storage ? DatabaseLootHandler.SuggestLoot(partyId, piece)).mapTo[LootSelectorResult].map(_.result)
}
