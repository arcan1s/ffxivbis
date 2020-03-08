package me.arcanis.ffxivbis.service

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabaseLootHandlerTest
  extends TestKit(ActorSystem("database-loot-handler", Settings.withRandomDatabase))
    with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  private val database = system.actorOf(impl.DatabaseImpl.props)
  private val timeout: FiniteDuration = 60 seconds

  override def beforeAll: Unit = {
    Await.result(Migration(system.settings.config), timeout)
    Await.result((database ? impl.DatabasePartyHandler.AddPlayer(Fixtures.playerEmpty))(timeout).mapTo[Int], timeout)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    Settings.clearDatabase(system.settings.config)
  }

  "database loot handler actor" must {

    "add loot" in {
      Fixtures.loot.foreach { piece =>
        database ! impl.DatabaseLootHandler.AddPieceTo(Fixtures.playerEmpty.playerId, piece)
        expectMsg(timeout, 1)
      }
    }

    "get party loot" in {
      database ! impl.DatabaseLootHandler.GetLoot(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyLootCompare(party, Fixtures.loot) => ()
      }
    }

    "get loot" in {
      database ! impl.DatabaseLootHandler.GetLoot(Fixtures.playerEmpty.partyId, Some(Fixtures.playerEmpty.playerId))
      expectMsgPF(timeout) {
        case party: Seq[_] if partyLootCompare(party, Fixtures.loot) => ()
      }
    }

    "remove loot" in {
      database ! impl.DatabaseLootHandler.RemovePieceFrom(Fixtures.playerEmpty.playerId, Fixtures.lootBody)
      expectMsg(timeout, 1)

      val newLoot = Fixtures.loot.filterNot(_ == Fixtures.lootBody)

      database ! impl.DatabaseLootHandler.GetLoot(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyLootCompare(party, newLoot) => ()
      }
    }

    "add same loot" in {
      database ! impl.DatabaseLootHandler.AddPieceTo(Fixtures.playerEmpty.playerId, Fixtures.lootBody)
      expectMsg(timeout, 1)

      Fixtures.loot.foreach { piece =>
        database ! impl.DatabaseLootHandler.AddPieceTo(Fixtures.playerEmpty.playerId, piece)
        expectMsg(timeout, 1)
      }

      database ! impl.DatabaseLootHandler.GetLoot(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyLootCompare(party, Fixtures.loot ++ Fixtures.loot) => ()
      }
    }

  }

  private def partyLootCompare[T](party: Seq[T], loot: Seq[Piece]): Boolean =
    Compare.seqEquals(party.foldLeft(Seq.empty[Piece]){ case (acc, player) =>
      acc ++ player.asInstanceOf[Player].loot.map(_.piece)
    }, loot)
}
