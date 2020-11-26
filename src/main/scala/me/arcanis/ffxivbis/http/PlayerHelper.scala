/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.http.api.v1.json.ApiAction
import me.arcanis.ffxivbis.models.{Party, PartyDescription, Player, PlayerId}
import me.arcanis.ffxivbis.service.impl.{DatabaseBiSHandler, DatabasePartyHandler}

import scala.concurrent.{ExecutionContext, Future}

trait PlayerHelper extends BisProviderHelper {

  def storage: ActorRef

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

  def doModifyPlayer(action: ApiAction.Value, player: Player)
                    (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    action match {
      case ApiAction.add => addPlayer(player)
      case ApiAction.remove => removePlayer(player.playerId)
    }

  def getPartyDescription(partyId: String)
                         (implicit executionContext: ExecutionContext, timeout: Timeout): Future[PartyDescription] =
    (storage ? DatabasePartyHandler.GetPartyDescription(partyId)).mapTo[PartyDescription]

  def getPlayers(partyId: String, maybePlayerId: Option[PlayerId])
                (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    maybePlayerId match {
      case Some(playerId) =>
        (storage ? DatabasePartyHandler.GetPlayer(playerId)).mapTo[Option[Player]].map(_.toSeq)
      case None =>
        (storage ? DatabasePartyHandler.GetParty(partyId)).mapTo[Party].map(_.players.values.toSeq)
    }

  def removePlayer(playerId: PlayerId)
                  (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabasePartyHandler.RemovePlayer(playerId)).mapTo[Int]

  def updateDescription(partyDescription: PartyDescription)
                       (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabasePartyHandler.UpdateParty(partyDescription)).mapTo[Int]
}
