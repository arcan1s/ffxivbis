package me.arcanis.ffxivbis.models

import scala.util.Try
import scala.util.matching.Regex

trait PlayerIdBase {
  def job: Job.Job
  def nick: String

  override def toString: String = s"$nick ($job)"
}

case class PlayerId(partyId: String, job: Job.Job, nick: String) extends PlayerIdBase

object PlayerId {
  def apply(partyId: String, maybeNick: Option[String], maybeJob: Option[String]): Option[PlayerId] =
    (maybeNick, maybeJob) match {
      case (Some(nick), Some(job)) => Try(PlayerId(partyId, Job.fromString(job), nick)).toOption
      case _ => None
    }

  private val prettyPlayerIdRegex: Regex = "^(.*) \\(([A-Z]{3})\\)$".r
  def apply(partyId: String, player: String): Option[PlayerId] = player match {
    case s"${prettyPlayerIdRegex(nick, job)}" => Try(PlayerId(partyId, Job.fromString(job), nick)).toOption
    case _ => None
  }
}
