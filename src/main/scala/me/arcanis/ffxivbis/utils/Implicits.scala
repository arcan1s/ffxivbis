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

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions
import scala.util.Try

object Implicits {

  implicit def getBooleanFromOptionString(maybeYes: Option[String]): Boolean = maybeYes.map(_.toLowerCase) match {
    case Some("yes" | "on") => true
    case _ => false
  }

  implicit def getFiniteDuration(duration: Duration): FiniteDuration =
    FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)

  implicit def getTimeout(duration: Duration): Timeout =
    FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)

  implicit class ConfigExtension(config: Config) {

    def getOptString(path: String): Option[String] =
      Try(config.getString(path)).toOption.filter(_.nonEmpty)
  }
}
