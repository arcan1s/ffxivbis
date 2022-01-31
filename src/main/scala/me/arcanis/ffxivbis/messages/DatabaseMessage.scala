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
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.service.LootSelector

sealed trait DatabaseMessage extends Message {

  def partyId: String

  def isReadOnly: Boolean
}

object DatabaseMessage {

  // bis handler
  trait BisDatabaseMessage extends DatabaseMessage

  case class AddPieceToBis(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  case class GetBiS(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]])
    extends BisDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class RemovePieceFromBiS(playerId: PlayerId, piece: Piece, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  case class RemovePiecesFromBiS(playerId: PlayerId, replyTo: ActorRef[Unit]) extends BisDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  // loot handler
  trait LootDatabaseMessage extends DatabaseMessage

  case class AddPieceTo(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean, replyTo: ActorRef[Unit])
    extends LootDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  case class GetLoot(partyId: String, playerId: Option[PlayerId], replyTo: ActorRef[Seq[Player]])
    extends LootDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class RemovePieceFrom(playerId: PlayerId, piece: Piece, isFreeLoot: Boolean, replyTo: ActorRef[Unit])
    extends LootDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  case class SuggestLoot(partyId: String, piece: Piece, replyTo: ActorRef[LootSelector.LootSelectorResult])
    extends LootDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  // party handler
  trait PartyDatabaseMessage extends DatabaseMessage

  case class AddPlayer(player: Player, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
    override val partyId: String = player.partyId
    override val isReadOnly: Boolean = false
  }

  case class GetParty(partyId: String, replyTo: ActorRef[Party]) extends PartyDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class GetPartyDescription(partyId: String, replyTo: ActorRef[PartyDescription]) extends PartyDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class GetPlayer(playerId: PlayerId, replyTo: ActorRef[Option[Player]]) extends PartyDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = true
  }

  case class RemovePlayer(playerId: PlayerId, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
    override val partyId: String = playerId.partyId
    override val isReadOnly: Boolean = false
  }

  case class UpdateParty(partyDescription: PartyDescription, replyTo: ActorRef[Unit]) extends PartyDatabaseMessage {
    override val partyId: String = partyDescription.partyId
    override val isReadOnly: Boolean = false
  }

  // user handler
  trait UserDatabaseMessage extends DatabaseMessage

  case class AddUser(user: User, isHashedPassword: Boolean, replyTo: ActorRef[Unit]) extends UserDatabaseMessage {
    override val partyId: String = user.partyId
    override val isReadOnly: Boolean = false
  }

  case class DeleteUser(partyId: String, username: String, replyTo: ActorRef[Unit]) extends UserDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class Exists(partyId: String, replyTo: ActorRef[Boolean]) extends UserDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class GetUser(partyId: String, username: String, replyTo: ActorRef[Option[User]]) extends UserDatabaseMessage {
    override val isReadOnly: Boolean = true
  }

  case class GetUsers(partyId: String, replyTo: ActorRef[Seq[User]]) extends UserDatabaseMessage {
    override val isReadOnly: Boolean = true
  }
}
