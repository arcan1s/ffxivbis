/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.impl

import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages.{AddPieceToBis, DatabaseMessage, GetBiS, RemovePieceFromBiS, RemovePiecesFromBiS}
import me.arcanis.ffxivbis.service.Database

trait DatabaseBiSHandler { this: Database  =>

  def bisHandler: DatabaseMessage.Handler = {
    case AddPieceToBis(playerId, piece, client) =>
      profile.insertPieceBiS(playerId, piece).foreach(_ => client ! ())
      Behaviors.same

    case GetBiS(partyId, maybePlayerId, client) =>
      getParty(partyId, withBiS = true, withLoot = false)
        .map(filterParty(_, maybePlayerId))
        .foreach(client ! _)
       Behaviors.same

    case RemovePieceFromBiS(playerId, piece, client) =>
      profile.deletePieceBiS(playerId, piece).foreach(_ => client ! ())
      Behaviors.same

    case RemovePiecesFromBiS(playerId, client) =>
      profile.deletePiecesBiS(playerId).foreach(_ => client ! ())
      Behaviors.same
  }
}

