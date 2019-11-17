package me.arcanis.ffxivbis.service

import java.time.Instant

import akka.actor.Actor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class RateLimiter extends Actor {
  import RateLimiter._
  import me.arcanis.ffxivbis.utils.Implicits._

  implicit private val executionContext: ExecutionContext = context.system.dispatcher

  private val maxRequestCount: Int = context.system.settings.config.getInt("me.arcanis.ffxivbis.web.limits.max-count")
  private val requestInterval: FiniteDuration = context.system.settings.config.getDuration("me.arcanis.ffxivbis.web.limits.interval")

  override def receive: Receive = handle(Map.empty)

  private def handle(cache: Map[String, Usage]): Receive = {
    case username: String =>
      val client = sender()
      val usage = if (cache.contains(username)) {
        cache(username)
      } else {
        context.system.scheduler.scheduleOnce(requestInterval, self, Reset(username))
        Usage()
      }
      context become handle(cache + (username -> usage.increment))

      val response = if (usage.count > maxRequestCount) Some(usage.left) else None
      client ! response

    case Reset(username) =>
      context become handle(cache - username)
  }
}

object RateLimiter {
  private case class Usage(count: Int = 0, since: Instant = Instant.now) {
    def increment: Usage = copy(count = count + 1)
    def left: Long = (Instant.now.toEpochMilli - since.toEpochMilli) / 1000
  }

  case class Reset(username: String)
}