/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

object Implicits {
  implicit def getBooleanFromOptionString(maybeYes: Option[String]): Boolean = maybeYes match {
    case Some("yes" | "on") => true
    case _ => false
  }

  implicit def getFiniteDuration(duration: Duration): Timeout =
    FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)
}
