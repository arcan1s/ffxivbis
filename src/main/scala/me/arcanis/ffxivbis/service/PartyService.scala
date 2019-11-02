/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.Party

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class PartyService(storage: ActorRef) extends Actor with StrictLogging {
  import PartyService._
  import me.arcanis.ffxivbis.utils.Implicits._

  private val cacheTimeout: FiniteDuration =
    context.system.settings.config.getDuration("me.arcanis.ffxivbis.settings.cache-timeout")
  implicit private val executionContext: ExecutionContext = context.dispatcher
  implicit private val timeout: Timeout =
    context.system.settings.config.getDuration("me.arcanis.ffxivbis.settings.request-timeout")

  override def receive: Receive = handle(Map.empty)

  private def handle(cache: Map[String, Party]): Receive = {
    case ForgetParty(partyId) =>
      context become handle(cache - partyId)

    case GetNewPartyId =>
      val client = sender()
      getPartyId.pipeTo(client)

    case req @ impl.DatabasePartyHandler.GetParty(partyId) =>
      val client = sender()
      val party = cache.get(partyId) match {
        case Some(party) => Future.successful(party)
        case None =>
          (storage ? req).mapTo[Party].map { party =>
            context become handle(cache + (partyId -> party))
            context.system.scheduler.scheduleOnce(cacheTimeout, self, ForgetParty(partyId))
            party
          }
      }
      party.pipeTo(client)

    case req: Database.DatabaseRequest =>
      self ! ForgetParty(req.partyId)
      storage.forward(req)
  }

  private def getPartyId: Future[String] = {
    val partyId = Party.randomPartyId
    (storage ? impl.DatabaseUserHandler.Exists(partyId)).mapTo[Boolean].flatMap {
      case true => getPartyId
      case false => Future.successful(partyId)
    }
  }
}

object PartyService {
  def props(storage: ActorRef): Props = Props(new PartyService(storage))

  case class ForgetParty(partyId: String)
  case object GetNewPartyId
}
