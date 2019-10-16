package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{Piece, Player, PlayerId}
import me.arcanis.ffxivbis.service.impl.DatabaseBiSHandler

import scala.concurrent.{ExecutionContext, Future}

class BiSHelper(storage: ActorRef, ariyala: ActorRef) extends AriyalaHelper(ariyala) {

  def addPieceBiS(playerId: PlayerId, piece: Piece)
                 (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseBiSHandler.AddPieceToBis(playerId, piece) }

  def bis(partyId: String, playerId: Option[PlayerId])
         (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Seq[Player]] =
    (storage ? DatabaseBiSHandler.GetBiS(partyId, playerId)).mapTo[Seq[Player]]

  def putBiS(playerId: PlayerId, link: String)
            (implicit executionContext: ExecutionContext, timeout: Timeout): Future[Unit] =
    downloadBiS(link, playerId.job).map(_.pieces.map(addPieceBiS(playerId, _)))

  def removePieceBiS(playerId: PlayerId, piece: Piece)
                    (implicit executionContext: ExecutionContext): Future[Unit] =
    Future { storage ! DatabaseBiSHandler.RemovePieceFromBiS(playerId, piece) }

}
