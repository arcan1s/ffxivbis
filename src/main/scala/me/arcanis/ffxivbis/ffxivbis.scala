package me.arcanis.ffxivbis

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ffxivbis {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val actorSystem = ActorSystem("ffxivbis", config)
    actorSystem.actorOf(Application.props, "ffxivbis")
  }
}
