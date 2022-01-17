/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import com.zaxxer.hikari.HikariDataSource
import me.arcanis.ffxivbis.models.{Loot, Piece, PlayerId}

import java.time.Instant
import javax.sql.DataSource
import scala.concurrent.{ExecutionContext, Future}

class DatabaseProfile(override val executionContext: ExecutionContext, config: Config)
  extends StrictLogging
  with BiSProfile
  with LootProfile
  with PartyProfile
  with PlayersProfile
  with UsersProfile {

  override val datasource: DataSource =
    try {
      val profile = DatabaseProfile.getSection(config)
      val dataSourceConfig = DatabaseConnection.getDataSourceConfig(profile)
      new HikariDataSource(dataSourceConfig)
    } catch {
      case exception: Exception =>
        logger.error("exception during storage initialization", exception)
        throw exception
    }

  // generic bis api
  def deletePieceBiS(playerId: PlayerId, piece: Piece): Future[Int] =
    byPlayerId(playerId, deletePieceBiSById(piece))

  def deletePiecesBiS(playerId: PlayerId): Future[Int] =
    byPlayerId(playerId, deletePiecesBiSById)

  def getPiecesBiS(playerId: PlayerId): Future[Seq[Loot]] =
    byPlayerId(playerId, getPiecesBiSById)

  def getPiecesBiS(partyId: String): Future[Seq[Loot]] =
    byPartyId(partyId, getPiecesBiSById)

  def insertPieceBiS(playerId: PlayerId, piece: Piece): Future[Int] =
    byPlayerId(playerId, insertPieceBiSById(piece))

  // generic loot api
  def deletePiece(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean): Future[Int] = {
    val loot = Loot(-1, piece, Instant.now, isFreeLoot)
    byPlayerId(playerId, deletePieceById(loot))
  }

  def getPieces(playerId: PlayerId): Future[Seq[Loot]] =
    byPlayerId(playerId, getPiecesById)

  def getPieces(partyId: String): Future[Seq[Loot]] =
    byPartyId(partyId, getPiecesById)

  def insertPiece(playerId: PlayerId, loot: Loot): Future[Int] =
    byPlayerId(playerId, insertPieceById(loot))

  private def byPartyId[T](partyId: String, callback: Seq[Long] => Future[T]): Future[T] =
    getPlayers(partyId).flatMap(callback)(executionContext)

  private def byPlayerId[T](playerId: PlayerId, callback: Long => Future[T]): Future[T] =
    getPlayer(playerId).flatMap {
      case Some(id) => callback(id)
      case None => Future.failed(DatabaseProfile.PlayerNotFound(playerId))
    }(executionContext)
}

object DatabaseProfile {

  case class PlayerNotFound(playerId: PlayerId) extends Exception(s"Could not find player $playerId")

  def getSection(config: Config): Config = {
    val section = config.getString("me.arcanis.ffxivbis.database.mode")
    config.getConfig(s"me.arcanis.ffxivbis.database.$section")
  }

  def now: Long = Instant.now.toEpochMilli
}
