/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
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
import me.arcanis.ffxivbis.models.{BiS, Player}
import me.arcanis.ffxivbis.service.database.Database

import scala.concurrent.Future

trait DatabasePartyHandler { this: Database =>

  def partyHandler(msg: PartyDatabaseMessage): Behavior[DatabaseMessage] =
    msg match {
      case AddPlayer(player, client) =>
        run(profile.insertPlayer(player))(_ => client ! ())
        Behaviors.same

      case GetParty(partyId, client) =>
        run(getParty(partyId, withBiS = true, withLoot = true))(client ! _)
        Behaviors.same

      case GetPartyDescription(partyId, client) =>
        run(profile.getPartyDescription(partyId))(client ! _)
        Behaviors.same

      case GetPlayer(playerId, client) =>
        run {
          profile
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
        }(client ! _)
        Behaviors.same

      case RemovePlayer(playerId, client) =>
        run(profile.deletePlayer(playerId))(_ => client ! ())
        Behaviors.same

      case UpdateParty(description, client) =>
        run(profile.insertPartyDescription(description))(_ => client ! ())
        Behaviors.same
    }
}
