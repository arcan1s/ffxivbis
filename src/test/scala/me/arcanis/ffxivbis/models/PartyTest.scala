package me.arcanis.ffxivbis.models

import me.arcanis.ffxivbis.Fixtures
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PartyTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  private val partyDescription = PartyDescription.empty(Fixtures.partyId)
  private val party =
    Party(partyDescription, Seq.empty, Map(Fixtures.playerEmpty.playerId -> Fixtures.playerEmpty))

  "party model" must {

    "accept player with same party id" in {
      noException should be thrownBy
        Party(partyDescription, Seq.empty, Map(Fixtures.playerEmpty.playerId -> Fixtures.playerEmpty))
    }

    "fail on multiple party ids" in {
      val anotherPlayer = Fixtures.playerEmpty.copy(partyId = Fixtures.partyId2)

      an [IllegalArgumentException] should be thrownBy
        Party(partyDescription, Seq.empty, Map(anotherPlayer.playerId -> anotherPlayer))
      an [IllegalArgumentException] should be thrownBy
        Party(partyDescription, Seq.empty, Map(Fixtures.playerEmpty.playerId -> Fixtures.playerEmpty, anotherPlayer.playerId -> anotherPlayer))
    }

    "return player list" in {
      Compare.seqEquals(party.getPlayers, Seq(Fixtures.playerEmpty)) shouldEqual true
    }

    "return player" in {
      party.player(Fixtures.playerEmpty.playerId) shouldEqual Some(Fixtures.playerEmpty)
    }

    "return empty on unknown player" in {
      party.player(Fixtures.playerEmpty.copy(partyId = Fixtures.partyId2).playerId) shouldEqual None
    }

    "add new player" in {
      val newParty = Party(partyDescription, Seq.empty, Map.empty)
      newParty.withPlayer(Fixtures.playerEmpty) shouldEqual party
    }

    "reject player with another party id" in {
      val anotherPlayer = Fixtures.playerEmpty.copy(partyId = Fixtures.partyId2)

      party.withPlayer(anotherPlayer) shouldEqual party
    }

  }
}
