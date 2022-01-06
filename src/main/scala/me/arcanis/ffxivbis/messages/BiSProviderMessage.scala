package me.arcanis.ffxivbis.messages

import akka.actor.typed.ActorRef
import me.arcanis.ffxivbis.models.{BiS, Job}

sealed trait BiSProviderMessage

case class DownloadBiS(link: String, job: Job.Job, replyTo: ActorRef[BiS]) extends BiSProviderMessage {

  require(link.nonEmpty && link.trim == link, "Link must be not empty and contain no spaces")
}
