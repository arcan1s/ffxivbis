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

import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages.{AddPieceTo, DatabaseMessage, GetLoot, RemovePieceFrom, SuggestLoot}
import me.arcanis.ffxivbis.models.Loot
import me.arcanis.ffxivbis.service.Database

trait DatabaseLootHandler { this: Database =>

  def lootHandler: DatabaseMessage.Handler = {
    case AddPieceTo(playerId, piece, isFreeLoot, client) =>
      val loot = Loot(-1, piece, Instant.now, isFreeLoot)
      profile.insertPiece(playerId, loot).foreach(_ => client ! ())
      Behaviors.same

    case GetLoot(partyId, maybePlayerId, client) =>
      getParty(partyId, withBiS = false, withLoot = true)
        .map(filterParty(_, maybePlayerId))
        .foreach(client ! _)
      Behaviors.same

    case RemovePieceFrom(playerId, piece, client) =>
      profile.deletePiece(playerId, piece).foreach(_ => client ! ())
      Behaviors.same

    case SuggestLoot(partyId, piece, client) =>
      getParty(partyId, withBiS = true, withLoot = true)
        .map(_.suggestLoot(piece))
        .foreach(client ! _)
      Behaviors.same
  }
}
