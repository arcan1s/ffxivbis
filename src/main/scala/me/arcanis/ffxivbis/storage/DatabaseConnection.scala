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
import com.zaxxer.hikari.HikariConfig

import java.sql.Connection
import java.util.Properties
import javax.sql.DataSource
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait DatabaseConnection {

  def datasource: DataSource

  def executionContext: ExecutionContext

  def withConnection[T](fn: Connection => T): Future[T] = {
    val promise = Promise[T]()

    executionContext.execute { () =>
      Try(datasource.getConnection) match {
        case Success(conn) =>
          try {
            val result = fn(conn)
            promise.trySuccess(result)
          } catch {
            case NonFatal(exception) => promise.tryFailure(exception)
          } finally
            conn.close()
        case Failure(exception) => promise.tryFailure(exception)
      }
    }

    promise.future
  }
}

object DatabaseConnection {

  def getDataSourceConfig(config: Config): HikariConfig = {
    val properties = new Properties()
    config.entrySet().asScala.map(_.getKey).foreach { key =>
      properties.setProperty(key, config.getString(key))
    }
    new HikariConfig(properties)
  }
}
