package me.arcanis.ffxivbis.service.database

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import me.arcanis.ffxivbis.messages.{AddPlayer, GetParty, GetPlayer, RemovePlayer}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.utils.Compare
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class DatabasePartyHandlerTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  private val database = testKit.spawn(Database())
  private val askTimeout = 60 seconds

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration(testKit.system.settings.config)
  }

  override def afterAll(): Unit = {
    Settings.clearDatabase(testKit.system.settings.config)
    super.afterAll()
  }

  "database party handler actor" must {

    "add player" in {
      val probe = testKit.createTestProbe[Unit]()
      database ! AddPlayer(Fixtures.playerEmpty, probe.ref)
      probe.expectMessage(askTimeout, ())
    }

    "get party" in {
      val probe = testKit.createTestProbe[Party]()
      database ! GetParty(Fixtures.partyId, probe.ref)

      val party = probe.expectMessageType[Party](askTimeout)
      Compare.seqEquals(party.getPlayers, Seq(Fixtures.playerEmpty)) shouldEqual true
    }

    "get player" in {
      val probe = testKit.createTestProbe[Option[Player]]()
      database ! GetPlayer(Fixtures.playerEmpty.playerId, probe.ref)
      probe.expectMessage(askTimeout, Some(Fixtures.playerEmpty))
    }

    "update player" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      val newPlayer = Fixtures.playerEmpty.copy(priority = 2)

      database ! AddPlayer(newPlayer, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Option[Player]]()
      database ! GetPlayer(newPlayer.playerId, probe.ref)
      probe.expectMessage(askTimeout, Some(newPlayer))

      val partyProbe = testKit.createTestProbe[Party]()
      database ! GetParty(Fixtures.partyId, partyProbe.ref)

      val party = partyProbe.expectMessageType[Party](askTimeout)
      Compare.seqEquals(party.getPlayers, Seq(newPlayer)) shouldEqual true
    }

    "remove player" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! RemovePlayer(Fixtures.playerEmpty.playerId, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Option[Player]]()
      database ! GetPlayer(Fixtures.playerEmpty.playerId, probe.ref)
      probe.expectMessage(askTimeout, None)
    }

  }
}
