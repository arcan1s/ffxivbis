/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.utils

import akka.util.Timeout
import com.typesafe.config.Config

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions
import scala.util.Try

object Implicits {

  implicit class ConfigExtension(config: Config) {

    def getFiniteDuration(path: String): FiniteDuration =
      FiniteDuration(config.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)

    def getOptString(path: String): Option[String] =
      Try(config.getString(path)).toOption.filter(_.nonEmpty)

    def getTimeout(path: String): Timeout = getFiniteDuration(path)
  }
}
