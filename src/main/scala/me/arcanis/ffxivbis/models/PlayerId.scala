package me.arcanis.ffxivbis.models

trait PlayerIdBase {
  def job: Job.Job
  def nick: String

  override def toString: String = s"$nick ($job)"
}

case class PlayerId(partyId: String, job: Job.Job, nick: String) extends PlayerIdBase

object PlayerId {
  def apply(partyId: String, maybeNick: Option[String], maybeJob: Option[String]): Option[PlayerId] =
    (maybeNick, maybeJob) match {
      case (Some(nick), Some(job)) => Some(PlayerId(partyId, Job.fromString(job), nick))
      case _ => None
    }
}
