package me.arcanis.ffxivbis.service

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{Player, PlayerId}
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.{ExecutionContext, Future}

trait Database extends Actor with StrictLogging {
  implicit def executionContext: ExecutionContext
  def profile: DatabaseProfile

  def filterParty(party: Party, maybePlayerId: Option[PlayerId]): Seq[Player] =
    (party, maybePlayerId) match {
      case (_, Some(playerId)) => party.player(playerId).map(Seq(_)).getOrElse(Seq.empty)
      case (_, _) => party.getPlayers
    }

  def getParty(partyId: String, withBiS: Boolean, withLoot: Boolean): Future[Party] =
    for {
      players <- profile.getParty(partyId)
      bis <- if (withBiS) profile.getPiecesBiS(partyId) else Future(Seq.empty)
      loot <- if (withLoot) profile.getPieces(partyId) else Future(Seq.empty)
    } yield Party(partyId, context.system.settings.config, players, bis, loot)
}
