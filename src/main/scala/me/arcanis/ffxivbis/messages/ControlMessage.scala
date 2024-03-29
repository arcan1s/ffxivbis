/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.messages

import akka.actor.typed.ActorRef
import me.arcanis.ffxivbis.models.Party

sealed trait ControlMessage extends Message

object ControlMessage {

  case class ForgetParty(partyId: String) extends ControlMessage

  case class GetNewPartyId(replyTo: ActorRef[String]) extends ControlMessage

  case class StoreParty(partyId: String, party: Party) extends ControlMessage
}
