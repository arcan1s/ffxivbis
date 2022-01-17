/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service.database

import com.typesafe.config.Config
import me.arcanis.ffxivbis.storage.DatabaseProfile
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.flywaydb.core.api.output.MigrateResult

import scala.util.Try

class Migration(config: Config) {

  import me.arcanis.ffxivbis.utils.Implicits._

  def performMigration(): Try[MigrateResult] = {
    val section = DatabaseProfile.getSection(config)

    val url = section.getString("jdbcUrl")
    val username = section.getOptString("username").orNull
    val password = section.getOptString("password").orNull

    val provider = url match {
      case s"jdbc:$p:$_" => p
      case other => throw new NotImplementedError(s"unknown could not parse jdbc url from $other")
    }

    val flywayConfiguration = new ClassicConfiguration
    flywayConfiguration.setLocationsAsStrings(s"db/migration/$provider")
    flywayConfiguration.setDataSource(url, username, password)
    val flyway = new Flyway(flywayConfiguration)

    Try(flyway.migrate())
  }
}

object Migration {

  def apply(config: Config): Try[MigrateResult] = new Migration(config).performMigration()
}
