/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.service

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector, Scheduler}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.messages._
import me.arcanis.ffxivbis.models.Party

import scala.concurrent.{ExecutionContext, Future}

class PartyService(context: ActorContext[Message], storage: ActorRef[DatabaseMessage])
  extends AbstractBehavior[Message](context)
  with StrictLogging {
  import me.arcanis.ffxivbis.utils.Implicits._

  private val cacheTimeout =
    context.system.settings.config.getFiniteDuration("me.arcanis.ffxivbis.settings.cache-timeout")
  implicit private val executionContext: ExecutionContext = {
    val selector = DispatcherSelector.fromConfig("me.arcanis.ffxivbis.default-dispatcher")
    context.system.dispatchers.lookup(selector)
  }
  implicit private val timeout: Timeout =
    context.system.settings.config.getTimeout("me.arcanis.ffxivbis.settings.request-timeout")
  implicit private val scheduler: Scheduler = context.system.scheduler

  override def onMessage(msg: Message): Behavior[Message] = handle(Map.empty)(msg)

  private def handle(cache: Map[String, Party]): Message.Handler = {
    case ControlMessage.ForgetParty(partyId) =>
      Behaviors.receiveMessage(handle(cache - partyId))

    case ControlMessage.GetNewPartyId(client) =>
      getPartyId.foreach(client ! _)
      Behaviors.same

    case ControlMessage.StoreParty(partyId, party) =>
      Behaviors.receiveMessage(handle(cache.updated(partyId, party)))

    case DatabaseMessage.GetParty(partyId, client) =>
      val party = cache.get(partyId) match {
        case Some(party) => Future.successful(party)
        case None =>
          storage.ask(ref => DatabaseMessage.GetParty(partyId, ref)).map { party =>
            context.self ! ControlMessage.StoreParty(partyId, party)
            context.system.scheduler
              .scheduleOnce(cacheTimeout, () => context.self ! ControlMessage.ForgetParty(partyId))
            party
          }
      }
      party.foreach(client ! _)
      Behaviors.same

    case req: DatabaseMessage =>
      storage ! req
      val result = if (req.isReadOnly) cache else cache - req.partyId
      Behaviors.receiveMessage(handle(result))
  }

  private def getPartyId: Future[String] = {
    val partyId = Party.randomPartyId
    storage.ask(ref => DatabaseMessage.Exists(partyId, ref)).flatMap {
      case true => getPartyId
      case false => Future.successful(partyId)
    }
  }
}

object PartyService {

  def apply(storage: ActorRef[DatabaseMessage]): Behavior[Message] =
    Behaviors.setup[Message](context => new PartyService(context, storage))
}
