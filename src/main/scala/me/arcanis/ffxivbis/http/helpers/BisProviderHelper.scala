/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.helpers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import me.arcanis.ffxivbis.messages.BiSProviderMessage
import me.arcanis.ffxivbis.messages.BiSProviderMessage._
import me.arcanis.ffxivbis.models.{BiS, Job}

import scala.concurrent.Future

trait BisProviderHelper {

  def provider: ActorRef[BiSProviderMessage]

  def downloadBiS(link: String, job: Job)(implicit timeout: Timeout, scheduler: Scheduler): Future[BiS] =
    provider.ask(DownloadBiS(link, job, _))
}
