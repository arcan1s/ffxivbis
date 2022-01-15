/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database.impl

import akka.actor.typed.scaladsl.Behaviors
import me.arcanis.ffxivbis.messages._
import me.arcanis.ffxivbis.models.{BiS, Player}
import me.arcanis.ffxivbis.service.database.Database

import scala.concurrent.Future

trait DatabasePartyHandler { this: Database =>

  def partyHandler: DatabaseMessage.Handler = {
    case AddPlayer(player, client) =>
      profile.insertPlayer(player).foreach(_ => client ! ())
      Behaviors.same

    case GetParty(partyId, client) =>
      getParty(partyId, withBiS = true, withLoot = true).foreach(client ! _)
      Behaviors.same

    case GetPartyDescription(partyId, client) =>
      profile.getPartyDescription(partyId).foreach(client ! _)
      Behaviors.same

    case GetPlayer(playerId, client) =>
      val player = profile
        .getPlayerFull(playerId)
        .flatMap { maybePlayerData =>
          Future.traverse(maybePlayerData.toSeq) { playerData =>
            for {
              bis <- profile.getPiecesBiS(playerId)
              loot <- profile.getPieces(playerId)
            } yield Player(
              playerData.id,
              playerId.partyId,
              playerId.job,
              playerId.nick,
              BiS(bis.map(_.piece)),
              loot,
              playerData.link,
              playerData.priority
            )
          }
        }
        .map(_.headOption)
      player.foreach(client ! _)
      Behaviors.same

    case RemovePlayer(playerId, client) =>
      profile.deletePlayer(playerId).foreach(_ => client ! ())
      Behaviors.same

    case UpdateParty(description, client) =>
      profile.insertPartyDescription(description).foreach(_ => client ! ())
      Behaviors.same
  }
}
