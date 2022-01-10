package me.arcanis.ffxivbis

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

import java.io.File

object Settings {
  def config(values: Map[String, AnyRef]): Config = {
    @scala.annotation.tailrec
    def replace(acc: Config, iter: List[(String, AnyRef)]): Config = iter match {
      case Nil => acc
      case (key -> value) :: tail => replace(acc.withValue(key, ConfigValueFactory.fromAnyRef(value)), tail)
    }

    val default = ConfigFactory.load()
    replace(default, values.toList)
  }

  def clearDatabase(config: Config): Unit =
    config.getString("me.arcanis.ffxivbis.database.sqlite.db.url").split(":")
      .lastOption.foreach { databasePath =>
        val databaseFile = new File(databasePath)
        if (databaseFile.exists)
          databaseFile.delete()
      }
  def randomDatabasePath: String = File.createTempFile("ffxivdb-",".db").toPath.toString
  def withRandomDatabase: Config =
    config(Map("me.arcanis.ffxivbis.database.sqlite.db.url" -> s"jdbc:sqlite:$randomDatabasePath"))
}
