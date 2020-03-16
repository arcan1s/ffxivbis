/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.impl

import java.time.Instant

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.{Loot, Piece, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabaseLootHandler { this: Database =>
  import DatabaseLootHandler._

  def lootHandler: Receive = {
    case AddPieceTo(playerId, piece, isFreeLoot) =>
      val client = sender()
      val loot = Loot(-1, piece, Instant.now, isFreeLoot)
      profile.insertPiece(playerId, loot).pipeTo(client)

    case GetLoot(partyId, maybePlayerId) =>
      val client = sender()
      getParty(partyId, withBiS = false, withLoot = true)
        .map(filterParty(_, maybePlayerId))
        .pipeTo(client)

    case RemovePieceFrom(playerId, piece) =>
      val client = sender()
      profile.deletePiece(playerId, piece).pipeTo(client)

    case SuggestLoot(partyId, piece) =>
      val client = sender()
      getParty(partyId, withBiS = true, withLoot = true).map(_.suggestLoot(piece)).pipeTo(client)
  }
}

object DatabaseLootHandler {
  case class AddPieceTo(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean) extends Database.DatabaseRequest {
    override def partyId: String = playerId.partyId
  }
  case class GetLoot(partyId: String, playerId: Option[PlayerId]) extends Database.DatabaseRequest
  case class RemovePieceFrom(playerId: PlayerId, piece: Piece) extends Database.DatabaseRequest {
    override def partyId: String = playerId.partyId
  }
  case class SuggestLoot(partyId: String, piece: Piece) extends Database.DatabaseRequest
}
