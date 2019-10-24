package me.arcanis.ffxivbis.service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.models.{Fixtures, Settings}
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabasePartyHandlerTest
  extends TestKit(ActorSystem("database-party-handler", Settings.withRandomDatabase))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  private val database = system.actorOf(impl.DatabaseImpl.props)
  private val timeout: FiniteDuration = 60 seconds

  override def beforeAll: Unit = {
    Await.result(Migration(system.settings.config), timeout)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    Settings.clearDatabase(system.settings.config)
  }

  "database party handler actor" must {

    "add player" in {
      database ! impl.DatabasePartyHandler.AddPlayer(Fixtures.playerEmpty)
      expectMsg(timeout, 1)
    }

    "get party" in {
      database ! impl.DatabasePartyHandler.GetParty(Fixtures.partyId)
      expectMsgPF(timeout) {
        case p: Party if Compare.seqEquals(p.getPlayers, Seq(Fixtures.playerEmpty)) => ()
      }
    }

    "get player" in {
      database ! impl.DatabasePartyHandler.GetPlayer(Fixtures.playerEmpty.playerId)
      expectMsg(timeout, Some(Fixtures.playerEmpty))
    }

    "update player" in {
      val newPlayer = Fixtures.playerEmpty.copy(priority = 2)

      database ! impl.DatabasePartyHandler.AddPlayer(newPlayer)
      expectMsg(timeout, 1)

      database ! impl.DatabasePartyHandler.GetPlayer(newPlayer.playerId)
      expectMsg(timeout, Some(newPlayer))

      database ! impl.DatabasePartyHandler.GetParty(Fixtures.partyId)
      expectMsgPF(timeout) {
        case p: Party if Compare.seqEquals(p.getPlayers, Seq(newPlayer)) => ()
      }
    }

    "remove player" in {
      database ! impl.DatabasePartyHandler.RemovePlayer(Fixtures.playerEmpty.playerId)
      expectMsg(timeout, 1)

      database ! impl.DatabasePartyHandler.GetPlayer(Fixtures.playerEmpty.playerId)
      expectMsg(timeout, None)
    }

  }
}
