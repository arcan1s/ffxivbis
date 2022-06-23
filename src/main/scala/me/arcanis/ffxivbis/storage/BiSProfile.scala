/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import anorm.SqlParser._
import anorm._
import me.arcanis.ffxivbis.models.{Job, Loot, Piece, PieceType}

import java.time.Instant
import scala.concurrent.Future

trait BiSProfile extends DatabaseConnection {

  private val loot: RowParser[Loot] =
    (long("player_id") ~ str("piece") ~ str("piece_type")
      ~ str("job") ~ long("created"))
      .map { case playerId ~ piece ~ pieceType ~ job ~ created =>
        Loot(
          playerId = playerId,
          piece = Piece(
            piece = piece,
            pieceType = PieceType.withName(pieceType),
            job = Job.withName(job)
          ),
          timestamp = Instant.ofEpochMilli(created),
          isFreeLoot = false,
        )
      }

  def deletePieceBiSById(piece: Piece)(playerId: Long): Future[Int] =
    withConnection { implicit conn =>
      SQL("""delete from bis
          | where player_id = {player_id}
          |   and piece = {piece}
          |   and piece_type = {piece_type}""".stripMargin)
        .on("player_id" -> playerId, "piece" -> piece.piece, "piece_type" -> piece.pieceType.toString)
        .executeUpdate()
    }

  def deletePiecesBiSById(playerId: Long): Future[Int] =
    withConnection { implicit conn =>
      SQL("""delete from bis where player_id = {player_id}""")
        .on("player_id" -> playerId)
        .executeUpdate()
    }

  def getPiecesBiSById(playerId: Long): Future[Seq[Loot]] = getPiecesBiSById(Seq(playerId))

  def getPiecesBiSById(playerIds: Seq[Long]): Future[Seq[Loot]] =
    if (playerIds.isEmpty) Future.successful(Seq.empty)
    else
      withConnection { implicit conn =>
        SQL("""select * from bis where player_id in ({player_ids})""")
          .on("player_ids" -> playerIds)
          .executeQuery()
          .as(loot.*)
      }

  def insertPieceBiSById(piece: Piece)(playerId: Long): Future[Int] =
    withConnection { implicit conn =>
      SQL("""insert into bis
          |  (player_id, piece, piece_type, job, created)
          | values
          |  ({player_id}, {piece}, {piece_type}, {job}, {created})
          | on conflict (player_id, piece, piece_type) do nothing""".stripMargin)
        .on(
          "player_id" -> playerId,
          "piece" -> piece.piece,
          "piece_type" -> piece.pieceType.toString,
          "job" -> piece.job.toString,
          "created" -> DatabaseProfile.now
        )
        .executeUpdate()
    }
}
