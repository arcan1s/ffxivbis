/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.impl

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.{BiS, Piece, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabaseBiSHandler { this: Database  =>
  import DatabaseBiSHandler._

  def bisHandler: Receive = {
    case AddPieceToBis(playerId, piece) =>
      val client = sender()
      profile.insertPieceBiS(playerId, piece).pipeTo(client)

    case GetBiS(partyId, maybePlayerId) =>
      val client = sender()
      getParty(partyId, withBiS = true, withLoot = false)
        .map(filterParty(_, maybePlayerId))
        .pipeTo(client)

    case RemovePieceFromBiS(playerId, piece) =>
      val client = sender()
      profile.deletePieceBiS(playerId, piece).pipeTo(client)
  }
}

object DatabaseBiSHandler {
  case class AddPieceToBis(playerId: PlayerId, piece: Piece)
  case class GetBiS(partyId: String, playerId: Option[PlayerId])
  case class RemovePieceFromBiS(playerId: PlayerId, piece: Piece)
}
