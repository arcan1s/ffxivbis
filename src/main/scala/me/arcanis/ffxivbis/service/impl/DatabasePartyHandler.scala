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
import me.arcanis.ffxivbis.models.{BiS, Player, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabasePartyHandler { this: Database  =>
  import DatabasePartyHandler._

  def partyHandler: Receive = {
    case AddPlayer(player) =>
      val client = sender()
      profile.insertPlayer(player).pipeTo(client)

    case GetParty(partyId) =>
      val client = sender()
      getParty(partyId, withBiS = true, withLoot = true).pipeTo(client)

    case GetPlayer(playerId) =>
      val client = sender()
      val player = for {
        bis <- profile.getPiecesBiS(playerId)
        loot <- profile.getPieces(playerId)
      } yield Player(playerId.partyId, playerId.job, playerId.nick,
        BiS(bis.map(_.piece)), loot.map(_.piece))
      player.pipeTo(client)

    case RemovePlayer(playerId) =>
      val client = sender()
      profile.deletePlayer(playerId).pipeTo(client)
  }
}

object DatabasePartyHandler {
  case class AddPlayer(player: Player)
  case class GetParty(partyId: String)
  case class GetPlayer(playerId: PlayerId)
  case class RemovePlayer(playerId: PlayerId)
}
