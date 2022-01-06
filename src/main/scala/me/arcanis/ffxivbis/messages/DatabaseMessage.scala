package me.arcanis.ffxivbis.messages

import akka.actor.typed.{ActorRef, Behavior}
import me.arcanis.ffxivbis.models.{Party, PartyDescription, Piece, Player, PlayerId, User}
import me.arcanis.ffxivbis.service.LootSelector

sealed trait DatabaseMessage extends Message {

  def partyId: String
}

object DatabaseMessage {

  type Handler = PartialFunction[DatabaseMessage, Behavior[DatabaseMessage]]
}

// bis handler
case class AddPieceToBis(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class GetBiS(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]]) extends DatabaseMessage

case class RemovePieceFromBiS(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class RemovePiecesFromBiS(playerId: PlayerId, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

// loot handler
case class AddPieceTo(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean, replyTo: ActorRef[Unit])
  extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class GetLoot(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]]) extends DatabaseMessage

case class RemovePieceFrom(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class SuggestLoot(partyId: String, piece: Piece, replyTo: ActorRef[LootSelector.LootSelectorResult])
  extends DatabaseMessage

// party handler
case class AddPlayer(player: Player, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = player.partyId
}

case class GetParty(partyId: String, replyTo: ActorRef[Party]) extends DatabaseMessage

case class GetPartyDescription(partyId: String, replyTo: ActorRef[PartyDescription]) extends DatabaseMessage

case class GetPlayer(playerId: PlayerId, replyTo: ActorRef[Option[Player]]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class RemovePlayer(playerId: PlayerId, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class UpdateParty(partyDescription: PartyDescription, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = partyDescription.partyId
}

// user handler
case class AddUser(user: User, isHashedPassword: Boolean, replyTo: ActorRef[Unit]) extends DatabaseMessage {
  override def partyId: String = user.partyId
}

case class DeleteUser(partyId: String, username: String, replyTo: ActorRef[Unit]) extends DatabaseMessage

case class Exists(partyId: String, replyTo: ActorRef[Boolean]) extends DatabaseMessage

case class GetUser(partyId: String, username: String, replyTo: ActorRef[Option[User]]) extends DatabaseMessage

case class GetUsers(partyId: String, replyTo: ActorRef[Seq[User]]) extends DatabaseMessage
