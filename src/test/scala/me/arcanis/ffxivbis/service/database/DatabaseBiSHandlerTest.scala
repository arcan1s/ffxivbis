package me.arcanis.ffxivbis.service.database

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import me.arcanis.ffxivbis.messages.{AddPieceToBis, AddPlayer, GetBiS, RemovePieceFromBiS}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabaseBiSHandlerTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  private val database = testKit.spawn(Database())
  private val askTimeout: FiniteDuration = 60 seconds

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration(testKit.system.settings.config)
    Await.result(database.ask(AddPlayer(Fixtures.playerEmpty, _))(askTimeout, testKit.scheduler), askTimeout)
  }

  override def afterAll(): Unit = {
    Settings.clearDatabase(testKit.system.settings.config)
    super.afterAll()
  }

  "database bis handler" must {

    "add pieces to bis" in {
      val probe = testKit.createTestProbe[Unit]()
      database ! AddPieceToBis(Fixtures.playerEmpty.playerId, Fixtures.lootBody, probe.ref)
      probe.expectMessage(askTimeout, ())

      database ! AddPieceToBis(Fixtures.playerEmpty.playerId, Fixtures.lootHands, probe.ref)
      probe.expectMessage(askTimeout, ())
    }

    "get party bis set" in {
      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetBiS(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyBiSCompare(party, Seq(Fixtures.lootBody, Fixtures.lootHands)) shouldEqual true
    }

    "get bis set" in {
      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetBiS(Fixtures.playerEmpty.partyId, Some(Fixtures.playerEmpty.playerId), probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyBiSCompare(party, Seq(Fixtures.lootBody, Fixtures.lootHands)) shouldEqual true
    }

    "remove piece from bis set" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! RemovePieceFromBiS(Fixtures.playerEmpty.playerId, Fixtures.lootBody, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetBiS(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyBiSCompare(party, Seq(Fixtures.lootHands)) shouldEqual true
    }

    "update piece in bis set" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      val newPiece = Hands(pieceType = PieceType.Savage, Job.DNC)

      database ! AddPieceToBis(Fixtures.playerEmpty.playerId, newPiece, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Seq[Player]]()
      database ! GetBiS(Fixtures.playerEmpty.partyId, None, probe.ref)

      val party = probe.expectMessageType[Seq[Player]](askTimeout)
      partyBiSCompare(party, Seq(Fixtures.lootHands, newPiece)) shouldEqual true
    }

  }

  private def partyBiSCompare[T](party: Seq[T], bis: Seq[Piece]): Boolean =
    Compare.seqEquals(party.foldLeft(Seq.empty[Piece]){ case (acc, player) => acc ++ player.asInstanceOf[Player].bis.pieces }, bis)
}
