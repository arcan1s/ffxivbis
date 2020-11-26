/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{BiS, Job}
import me.arcanis.ffxivbis.service.bis.BisProvider

import scala.concurrent.{ExecutionContext, Future}

trait BisProviderHelper {

  def ariyala: ActorRef

  def downloadBiS(link: String, job: Job.Job)
                 (implicit executionContext: ExecutionContext, timeout: Timeout): Future[BiS] =
    (ariyala ? BisProvider.GetBiS(link, job)).mapTo[BiS]
}
