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
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId}

import scala.concurrent.{ExecutionContext, Future}

trait BiSHelper extends BisProviderHelper {

  def storage: ActorRef[Message]

  def addPieceBiS(playerId: PlayerId, piece: Piece)(implicit timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(AddPieceToBis(playerId, piece.withJob(playerId.job), _))

  def bis(partyId: String, playerId: Option[PlayerId])(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Seq[Player]] =
    storage.ask(GetBiS(partyId, playerId, _))

  def doModifyBiS(action: ApiAction.Value, playerId: PlayerId, piece: Piece)(implicit
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    action match {
      case ApiAction.add => addPieceBiS(playerId, piece)
      case ApiAction.remove => removePieceBiS(playerId, piece)
    }

  def putBiS(playerId: PlayerId, link: String)(implicit
    executionContext: ExecutionContext,
    timeout: Timeout,
    scheduler: Scheduler
  ): Future[Unit] =
    storage
      .ask(RemovePiecesFromBiS(playerId, _))
      .flatMap { _ =>
        downloadBiS(link, playerId.job)
          .flatMap { bis =>
            Future.traverse(bis.pieces)(addPieceBiS(playerId, _))
          }
      }
      .flatMap(_ => storage.ask(UpdateBiSLink(playerId, link, _)))

  def removePieceBiS(playerId: PlayerId, piece: Piece)(implicit timeout: Timeout, scheduler: Scheduler): Future[Unit] =
    storage.ask(RemovePieceFromBiS(playerId, piece, _))

}
