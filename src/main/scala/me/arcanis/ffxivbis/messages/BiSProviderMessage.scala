/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.messages

import akka.actor.typed.ActorRef
import me.arcanis.ffxivbis.models.{BiS, Job}

sealed trait BiSProviderMessage

object BiSProviderMessage {

  case class DownloadBiS(link: String, job: Job, replyTo: ActorRef[BiS]) extends BiSProviderMessage {

    require(link.nonEmpty && link.trim == link, "Link must be not empty and contain no spaces")
  }
}
