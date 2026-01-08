/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database.impl

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages.DatabaseMessage
import me.arcanis.ffxivbis.messages.DatabaseMessage._
import me.arcanis.ffxivbis.models.Loot
import me.arcanis.ffxivbis.service.database.Database

import java.time.Instant

trait DatabaseLootHandler { this: Database =>

  def lootHandler(msg: LootDatabaseMessage): Behavior[DatabaseMessage] =
    msg match {
      case AddPieceTo(playerId, piece, isFreeLoot, client) =>
        val loot = Loot(-1, piece, Instant.now, isFreeLoot)
        run(profile.insertPiece(playerId, loot))(_ => client ! ())
        Behaviors.same

      case GetLoot(partyId, maybePlayerId, client) =>
        run {
          getParty(partyId, withBiS = false, withLoot = true)
            .map(filterParty(_, maybePlayerId))
        }(client ! _)
        Behaviors.same

      case RemovePieceFrom(playerId, piece, isFreeLoot, client) =>
        run(profile.deletePiece(playerId, piece, isFreeLoot))(_ => client ! ())
        Behaviors.same

      case SuggestLoot(partyId, piece, client) =>
        run {
          getParty(partyId, withBiS = true, withLoot = true)
            .map(_.suggestLoot(Seq(piece)))
        }(client ! _)
        Behaviors.same

      case SuggestMultiLoot(partyId, pieces, client) =>
        run {
          getParty(partyId, withBiS = true, withLoot = true)
            .map(_.suggestLoot(pieces))
        }(client ! _)
        Behaviors.same
    }
}
