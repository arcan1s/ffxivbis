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

import scala.concurrent.Future

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
      val player = profile.getPlayerFull(playerId).flatMap { maybePlayerData =>
        Future.traverse(maybePlayerData.toSeq) { playerData =>
          for {
            bis <- profile.getPiecesBiS(playerId)
            loot <- profile.getPieces(playerId)
          } yield Player(playerId.partyId, playerId.job, playerId.nick,
            BiS(bis.map(_.piece)), loot.map(_.piece),
            playerData.link, playerData.priority)
        }
      }.map(_.headOption)
      player.pipeTo(client)

    case RemovePlayer(playerId) =>
      val client = sender()
      profile.deletePlayer(playerId).pipeTo(client)
  }
}

object DatabasePartyHandler {
  case class AddPlayer(player: Player) extends Database.DatabaseRequest {
    override def partyId: String = player.partyId
  }
  case class GetParty(partyId: String) extends Database.DatabaseRequest
  case class GetPlayer(playerId: PlayerId) extends Database.DatabaseRequest {
    override def partyId: String = playerId.partyId
  }
  case class RemovePlayer(playerId: PlayerId) extends Database.DatabaseRequest {
    override def partyId: String = playerId.partyId
  }
}
