package me.arcanis.ffxivbis.service.impl

import akka.pattern.pipe
import me.arcanis.ffxivbis.models.{Piece, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabaseLootHandler { this: Database =>
  import DatabaseLootHandler._

  def lootHandler: Receive = {
    case AddPieceTo(playerId, piece) =>
      val client = sender()
      profile.insertPiece(playerId, piece).pipeTo(client)

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
  case class AddPieceTo(playerId: PlayerId, piece: Piece)
  case class GetLoot(partyId: String, playerId: Option[PlayerId])
  case class RemovePieceFrom(playerId: PlayerId, piece: Piece)
  case class SuggestLoot(partyId: String, piece: Piece)
}
