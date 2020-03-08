/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{Party, Player, PlayerId}
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.{ExecutionContext, Future}

trait Database extends Actor with StrictLogging {
  implicit def executionContext: ExecutionContext
  def profile: DatabaseProfile

  override def postStop(): Unit = {
    profile.db.close()
    super.postStop()
  }

  def filterParty(party: Party, maybePlayerId: Option[PlayerId]): Seq[Player] =
    maybePlayerId match {
      case Some(playerId) => party.player(playerId).map(Seq(_)).getOrElse(Seq.empty)
      case _ => party.getPlayers
    }

  def getParty(partyId: String, withBiS: Boolean, withLoot: Boolean): Future[Party] =
    for {
      partyDescription <- profile.getPartyDescription(partyId)
      players <- profile.getParty(partyId)
      bis <- if (withBiS) profile.getPiecesBiS(partyId) else Future(Seq.empty)
      loot <- if (withLoot) profile.getPieces(partyId) else Future(Seq.empty)
    } yield Party(partyDescription, context.system.settings.config, players, bis, loot)
}

object Database {
  trait DatabaseRequest {
    def partyId: String
  }
}
