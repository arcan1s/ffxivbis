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
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId}
import me.arcanis.ffxivbis.service.impl.DatabaseBiSHandler

import scala.concurrent.{ExecutionContext, Future}

class BiSHelper(storage: ActorRef, ariyala: ActorRef) extends AriyalaHelper(ariyala) {

  def addPieceBiS(playerId: PlayerId, piece: Piece)
                 (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseBiSHandler.AddPieceToBis(playerId, piece)).mapTo[Int]

  def bis(partyId: String, playerId: Option[PlayerId])
         (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    (storage ? DatabaseBiSHandler.GetBiS(partyId, playerId)).mapTo[Seq[Player]]

  def putBiS(playerId: PlayerId, link: String)
            (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] =
    downloadBiS(link, playerId.job).map(_.pieces.map(addPieceBiS(playerId, _)))

  def removePieceBiS(playerId: PlayerId, piece: Piece)
                    (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Int] =
    (storage ? DatabaseBiSHandler.RemovePieceFromBiS(playerId, piece)).mapTo[Int]

}
