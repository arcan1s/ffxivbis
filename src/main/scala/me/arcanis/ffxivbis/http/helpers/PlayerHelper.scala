/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.helpers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import me.arcanis.ffxivbis.http.api.v1.json.ApiAction
import me.arcanis.ffxivbis.messages.DatabaseMessage._
import me.arcanis.ffxivbis.messages.Message
import me.arcanis.ffxivbis.models.{PartyDescription, Player, PlayerId}

import scala.concurrent.{ExecutionContext, Future}

trait PlayerHelper extends BisProviderHelper {

  def storage: ActorRef[Message]

  def addPlayer(
    player: Player
  )(implicit executionContext: ExecutionContext, timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage
      .ask(ref => AddPlayer(player, ref))
      .map { _ =>
        player.link.map(_.trim).filter(_.nonEmpty) match {
          case Some(link) =>
            downloadBiS(link, player.job)
              .map { bis =>
                bis.pieces.map(piece => storage.ask(AddPieceToBis(player.playerId, piece, _)))
              }
              .flatMap(_ => storage.ask(UpdateBiSLink(player.playerId, link, _)))
          case None => Future.successful(())
        }
      }
      .flatten

  def doModifyPlayer(action: ApiAction.Value, player: Player)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    action match {
      case ApiAction.add => addPlayer(player)
      case ApiAction.remove => removePlayer(player.playerId)
    }

  def getPartyDescription(partyId: String)(implicit timeout: Timeout, scheduler: Scheduler): Future[PartyDescription] =
    storage.ask(GetPartyDescription(partyId, _))

  def getPlayers(partyId: String, maybePlayerId: Option[PlayerId])(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Seq[Player]] =
    maybePlayerId match {
      case Some(playerId) =>
        storage.ask(GetPlayer(playerId, _)).map(_.toSeq)
      case None =>
        storage.ask(GetParty(partyId, _)).map(_.players.values.toSeq)
    }

  def removePlayer(playerId: PlayerId)(implicit timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(RemovePlayer(playerId, _))

  def updateDescription(
    partyDescription: PartyDescription
  )(implicit executionContext: ExecutionContext, timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(UpdateParty(partyDescription, _))
}
