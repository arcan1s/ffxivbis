/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import me.arcanis.ffxivbis.models.{Job, Loot, Piece}
import slick.lifted.{ForeignKeyQuery, Index, PrimaryKey}

import scala.concurrent.Future

trait BiSProfile { this: DatabaseProfile =>
  import dbConfig.profile.api._

  case class BiSRep(playerId: Long, created: Long, piece: String, isTome: Int, job: String) {
    def toLoot: Loot = Loot(playerId, Piece(piece, isTome == 1, Job.withName(job)))
  }
  object BiSRep {
    def fromPiece(playerId: Long, piece: Piece) =
      BiSRep(playerId, DatabaseProfile.now, piece.piece, if (piece.isTome) 1 else 0,
        piece.job.toString)
  }

  class BiSPieces(tag: Tag) extends Table[BiSRep](tag, "bis") {
    def playerId: Rep[Long] = column[Long]("player_id", O.PrimaryKey)
    def created: Rep[Long] = column[Long]("created")
    def piece: Rep[String] = column[String]("piece", O.PrimaryKey)
    def isTome: Rep[Int] = column[Int]("is_tome")
    def job: Rep[String] = column[String]("job")

    def * =
      (playerId, created, piece, isTome, job) <> ((BiSRep.apply _).tupled, BiSRep.unapply)

    def fkPlayerId: ForeignKeyQuery[Players, PlayerRep] =
      foreignKey("player_id", playerId, playersTable)(_.playerId, onDelete = ForeignKeyAction.Cascade)
  }

  def deletePieceBiSById(piece: Piece)(playerId: Long): Future[Int] =
    db.run(pieceBiS(BiSRep.fromPiece(playerId, piece)).delete)
  def getPiecesBiSById(playerId: Long): Future[Seq[Loot]] = getPiecesBiSById(Seq(playerId))
  def getPiecesBiSById(playerIds: Seq[Long]): Future[Seq[Loot]] =
    db.run(piecesBiS(playerIds).result).map(_.map(_.toLoot))
  def insertPieceBiSById(piece: Piece)(playerId: Long): Future[Int] =
    db.run(bisTable.insertOrUpdate(BiSRep.fromPiece(playerId, piece)))

  private def pieceBiS(piece: BiSRep) =
    piecesBiS(Seq(piece.playerId)).filter(_.piece === piece.piece)
  private def piecesBiS(playerIds: Seq[Long]) =
    bisTable.filter(_.playerId.inSet(playerIds.toSet))
}