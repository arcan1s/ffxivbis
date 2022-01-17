/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.messages

import akka.actor.typed.{ActorRef, Behavior}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.service.LootSelector

sealed trait DatabaseMessage extends Message {

  def partyId: String
}

object DatabaseMessage {

  type Handler = PartialFunction[DatabaseMessage, Behavior[DatabaseMessage]]
}

// bis handler
trait BisDatabaseMessage extends DatabaseMessage

case class AddPieceToBis(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class GetBiS(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]])
  extends BisDatabaseMessage

case class RemovePieceFromBiS(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class RemovePiecesFromBiS(playerId: PlayerId, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
  override def partyId: String = playerId.partyId
}

// loot handler
trait LootDatabaseMessage extends DatabaseMessage

case class AddPieceTo(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean, replyTo: ActorRef[Unit])
  extends LootDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class GetLoot(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]])
  extends LootDatabaseMessage

case class RemovePieceFrom(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean, replyTo: ActorRef[Unit])
  extends LootDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class SuggestLoot(partyId: String, piece: Piece, replyTo: ActorRef[LootSelector.LootSelectorResult])
  extends LootDatabaseMessage

// party handler
trait PartyDatabaseMessage extends DatabaseMessage

case class AddPlayer(player: Player, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
  override def partyId: String = player.partyId
}

case class GetParty(partyId: String, replyTo: ActorRef[Party]) extends PartyDatabaseMessage

case class GetPartyDescription(partyId: String, replyTo: ActorRef[PartyDescription]) extends PartyDatabaseMessage

case class GetPlayer(playerId: PlayerId, replyTo: ActorRef[Option[Player]]) extends PartyDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class RemovePlayer(playerId: PlayerId, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
  override def partyId: String = playerId.partyId
}

case class UpdateParty(partyDescription: PartyDescription, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
  override def partyId: String = partyDescription.partyId
}

// user handler
trait UserDatabaseMessage extends DatabaseMessage

case class AddUser(user: User, isHashedPassword: Boolean, replyTo: ActorRef[Unit]) extends UserDatabaseMessage {
  override def partyId: String = user.partyId
}

case class DeleteUser(partyId: String, username: String, replyTo: ActorRef[Unit]) extends UserDatabaseMessage

case class Exists(partyId: String, replyTo: ActorRef[Boolean]) extends UserDatabaseMessage

case class GetUser(partyId: String, username: String, replyTo: ActorRef[Option[User]]) extends UserDatabaseMessage

case class GetUsers(partyId: String, replyTo: ActorRef[Seq[User]]) extends UserDatabaseMessage
