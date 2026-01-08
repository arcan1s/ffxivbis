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
import me.arcanis.ffxivbis.service.database.Database

trait DatabaseBiSHandler { this: Database =>

  def bisHandler(msg: BisDatabaseMessage): Behavior[DatabaseMessage] =
    msg match {
      case AddPieceToBis(playerId, piece, client) =>
        run(profile.insertPieceBiS(playerId, piece))(_ => client ! ())
        Behaviors.same

      case GetBiS(partyId, maybePlayerId, client) =>
        run {
          getParty(partyId, withBiS = true, withLoot = false)
            .map(filterParty(_, maybePlayerId))
        }(client ! _)
        Behaviors.same

      case RemovePieceFromBiS(playerId, piece, client) =>
        run(profile.deletePieceBiS(playerId, piece))(_ => client ! ())
        Behaviors.same

      case RemovePiecesFromBiS(playerId, client) =>
        run(profile.deletePiecesBiS(playerId))(_ => client ! ())
        Behaviors.same
    }
}
