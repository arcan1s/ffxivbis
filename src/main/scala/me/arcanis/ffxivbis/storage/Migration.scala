/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.storage

import com.typesafe.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.ClassicConfiguration

import scala.concurrent.Future
import scala.util.Try

class Migration(config: Config) {

  def performMigration(): Try[Boolean] = {
    val section = DatabaseProfile.getSection(config)

    val url = section.getString("db.url")
    val username = section.getString("db.user")
    val password = section.getString("db.password")

    val provider = url match {
      case s"jdbc:$p:$_" => p
      case other => throw new NotImplementedError(s"unknown could not parse jdbc url from $other")
    }

    val flywayConfiguration = new ClassicConfiguration
    flywayConfiguration.setLocationsAsStrings(s"db/migration/$provider")
    flywayConfiguration.setDataSource(url, username, password)
    val flyway = new Flyway(flywayConfiguration)

    Try(flyway.migrate().success)
  }
}

object Migration {

  def apply(config: Config): Try[Boolean] = new Migration(config).performMigration()
}
