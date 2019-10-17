/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
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
