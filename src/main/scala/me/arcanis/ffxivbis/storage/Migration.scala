package me.arcanis.ffxivbis.storage

import com.typesafe.config.Config
import org.flywaydb.core.Flyway

import scala.concurrent.Future

class Migration(config: Config) {
  def performMigration(): Future[Int] = {
    val section = DatabaseProfile.getSection(config)

    val url = section.getString("db.url")
    val username = section.getString("db.user")
    val password = section.getString("db.password")

    val flyway = Flyway.configure().dataSource(url, username, password).load()
    Future.successful(flyway.migrate())
  }
}

object Migration {
  def apply(config: Config): Future[Int] = new Migration(config).performMigration()
}
