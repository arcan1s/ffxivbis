package me.arcanis.ffxivbis.messages

import akka.actor.typed.ActorRef
import me.arcanis.ffxivbis.models.Party

case class ForgetParty(partyId: String) extends Message

case class GetNewPartyId(replyTo: ActorRef[String]) extends Message

case class StoreParty(partyId: String, party: Party) extends Message
