package me.arcanis.ffxivbis.service.impl

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.{BiS, Piece, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabaseBiSHandler { this: Database  =>
  import DatabaseBiSHandler._

  def bisHandler: Receive = {
    case AddPieceToBis(playerId, piece) =>
      profile.insertPieceBiS(playerId, piece)

    case GetBiS(partyId, maybePlayerId) =>
      val client = sender()
      getParty(partyId, withBiS = true, withLoot = false)
        .map(filterParty(_, maybePlayerId))
        .pipeTo(client)

    case RemovePieceFromBiS(playerId, piece) =>
      profile.deletePieceBiS(playerId, piece)
  }
}

object DatabaseBiSHandler {
  case class AddPieceToBis(playerId: PlayerId, piece: Piece)
  case class GetBiS(partyId: String, playerId: Option[PlayerId])
  case class RemovePieceFromBiS(playerId: PlayerId, piece: Piece)
}
