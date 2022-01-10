/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

object Configuration {

  def load(): Config = {
    val root = ConfigFactory.load()
    root
      .withValue(
        "akka.http.server.transparent-head-requests",
        ConfigValueFactory.fromAnyRef(root.getBoolean("me.arcanis.ffxivbis.web.enable-head-requests"))
      )
  }
}
