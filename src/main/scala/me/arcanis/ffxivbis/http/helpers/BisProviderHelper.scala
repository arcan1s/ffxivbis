package me.arcanis.ffxivbis.http.helpers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import me.arcanis.ffxivbis.messages.{BiSProviderMessage, DownloadBiS}
import me.arcanis.ffxivbis.models.{BiS, Job}

import scala.concurrent.Future

trait BisProviderHelper {

  def provider: ActorRef[BiSProviderMessage]

  def downloadBiS(link: String, job: Job.Job)(implicit timeout: Timeout, scheduler: Scheduler): Future[BiS] =
    provider.ask(DownloadBiS(link, job, _))
}
