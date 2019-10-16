package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{Player, PlayerId}
import me.arcanis.ffxivbis.service.Party
import me.arcanis.ffxivbis.service.impl.{DatabaseBiSHandler, DatabasePartyHandler}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PlayerHelper(storage: ActorRef, ariyala: ActorRef) extends AriyalaHelper(ariyala) {

  def addPlayer(player: Player)
               (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabasePartyHandler.AddPlayer(player)).mapTo[Int].map { res =>
      player.link match {
        case Some(link) =>
          downloadBiS(link, player.job).map { bis =>
            bis.pieces.map(storage ? DatabaseBiSHandler.AddPieceToBis(player.playerId, _))
          }.map(_ => res)
        case None => Future.successful(res)
      }
    }.flatten

  def getPlayers(partyId: String, maybePlayerId: Option[PlayerId])
                (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    maybePlayerId match {
      case Some(playerId) =>
        (storage ? DatabasePartyHandler.GetPlayer(playerId)).mapTo[Player].map(Seq(_))
      case None =>
        (storage ? DatabasePartyHandler.GetParty(partyId)).mapTo[Party].map(_.players.values.toSeq)
    }

  def removePlayer(playerId: PlayerId)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabasePartyHandler.RemovePlayer(playerId)).mapTo[Int]
}
