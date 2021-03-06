/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector, Scheduler}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages.{DatabaseMessage, Exists, ForgetParty, GetNewPartyId, GetParty, Message, StoreParty}
import me.arcanis.ffxivbis.models.Party

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class PartyService(context: ActorContext[Message], storage: ActorRef[DatabaseMessage])
  extends AbstractBehavior[Message](context) with StrictLogging {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val cacheTimeout: FiniteDuration =
    context.system.settings.config.getDuration("me.arcanis.ffxivbis.settings.cache-timeout")
  implicit private val executionContext: ExecutionContext = {
    val selector = DispatcherSelector.fromConfig("me.arcanis.ffxivbis.default-dispatcher")
    context.system.dispatchers.lookup(selector)
  }
  implicit private val timeout: Timeout =
    context.system.settings.config.getDuration("me.arcanis.ffxivbis.settings.request-timeout")
  implicit private val scheduler: Scheduler = context.system.scheduler

  override def onMessage(msg: Message): Behavior[Message] = handle(Map.empty)(msg)

  private def handle(cache: Map[String, Party]): Message.Handler = {
    case ForgetParty(partyId) =>
      Behaviors.receiveMessage(handle(cache - partyId))

    case GetNewPartyId(client) =>
      getPartyId.foreach(client ! _)
      Behaviors.same

    case StoreParty(partyId, party) =>
      Behaviors.receiveMessage(handle(cache.updated(partyId, party)))

    case GetParty(partyId, client) =>
      val party = cache.get(partyId) match {
        case Some(party) => Future.successful(party)
        case None =>
          storage.ask(ref => GetParty(partyId, ref)).map { party =>
            context.self ! StoreParty(partyId, party)
            context.system.scheduler.scheduleOnce(cacheTimeout, () => context.self ! ForgetParty(partyId))
            party
          }
      }
      party.foreach(client ! _)
      Behaviors.same

    case req: DatabaseMessage =>
      storage ! req
      Behaviors.receiveMessage(handle(cache - req.partyId))
  }

  private def getPartyId: Future[String] = {
    val partyId = Party.randomPartyId
    storage.ask(ref => Exists(partyId, ref)).flatMap {
      case true => getPartyId
      case false => Future.successful(partyId)
    }
  }
}

object PartyService {

  def apply(storage: ActorRef[DatabaseMessage]): Behavior[Message] =
    Behaviors.setup[Message](context => new PartyService(context, storage))
}
