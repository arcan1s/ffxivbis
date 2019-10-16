package me.arcanis.ffxivbis.utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

object Implicits {
  implicit def getFiniteDuration(duration: Duration): Timeout =
    FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)
}
