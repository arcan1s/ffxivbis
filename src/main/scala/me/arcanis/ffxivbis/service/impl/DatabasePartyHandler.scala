package me.arcanis.ffxivbis.service.impl

import akka.actor.Actor
import akka.pattern.pipe
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.models.{BiS, Player, PlayerId}
import me.arcanis.ffxivbis.service.Database

trait DatabasePartyHandler { this: Actor with StrictLogging with Database  =>
  import DatabasePartyHandler._

  def partyHandler: Receive = {
    case AddPlayer(player) =>
      profile.insertPlayer(player)

    case GetParty(partyId) =>
      val client = sender()
      getParty(partyId, withBiS = true, withLoot = true).pipeTo(client)

    case GetPlayer(playerId) =>
      val client = sender()
      val player = for {
        bis <- profile.getPiecesBiS(playerId)
        loot <- profile.getPieces(playerId)
      } yield Player(playerId.partyId, playerId.job, playerId.nick,
        BiS(bis.map(_.piece)), loot.map(_.piece))
      player.pipeTo(client)

    case RemovePlayer(playerId) =>
      profile.deletePlayer(playerId)
  }
}

object DatabasePartyHandler {
  case class AddPlayer(player: Player)
  case class GetParty(partyId: String)
  case class GetPlayer(playerId: PlayerId)
  case class RemovePlayer(playerId: PlayerId)
}
