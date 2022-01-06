package me.arcanis.ffxivbis.service.database

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import me.arcanis.ffxivbis.messages.{AddPieceTo, AddPlayer, GetLoot, RemovePieceFrom}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabaseLootHandlerTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  private val database = testKit.spawn(Database())
  private val askTimeout = 60 seconds

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration(testKit.system.settings.config)
    Await.result(database.ask(AddPlayer(Fixtures.playerEmpty, _))(askTimeout, testKit.scheduler), askTimeout)
  }

  override def afterAll(): Unit = {
    Settings.clearDatabase(testKit.system.settings.config)
    super.afterAll()
  }

  "database loot handler actor" must {

    "add loot" in {
      val probe = testKit.createTestProbe[Unit]()
      Fixtures.loot.foreach { piece =>
        database ! AddPieceTo(Fixtures.playerEmpty.playerId, piece, isFreeLoot = false, probe.ref)
        probe.expectMessage(askTimeout, ())
      }
    }

    "get party loot" in {
      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetLoot(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyLootCompare(party, Fixtures.loot) shouldEqual true
    }

    "get loot" in {
      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetLoot(Fixtures.playerEmpty.partyId, Some(Fixtures.playerEmpty.playerId), probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyLootCompare(party, Fixtures.loot) shouldEqual true
    }

    "remove loot" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! RemovePieceFrom(Fixtures.playerEmpty.playerId, Fixtures.lootBody, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val newLoot = Fixtures.loot.filterNot(_ == Fixtures.lootBody)

      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetLoot(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyLootCompare(party, newLoot) shouldEqual true
    }

    "add same loot" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! AddPieceTo(Fixtures.playerEmpty.playerId, Fixtures.lootBody, isFreeLoot = false, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      Fixtures.loot.foreach { piece =>
        database ! AddPieceTo(Fixtures.playerEmpty.playerId, piece, isFreeLoot = false, updateProbe.ref)
        updateProbe.expectMessage(askTimeout, ())
      }

      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetLoot(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyLootCompare(party, Fixtures.loot ++ Fixtures.loot) shouldEqual true
    }

  }

  private def partyLootCompare[T](party: Seq[T], loot: Seq[Piece]): Boolean =
    Compare.seqEquals(party.foldLeft(Seq.empty[Piece]){ case (acc, player) =>
      acc ++ player.asInstanceOf[Player].loot.map(_.piece)
    }, loot)
}
