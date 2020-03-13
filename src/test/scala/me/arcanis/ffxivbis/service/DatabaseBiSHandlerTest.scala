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

class DatabaseBiSHandlerTest
  extends TestKit(ActorSystem("database-bis-handler", Settings.withRandomDatabase))
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

  "database bis handler" must {

    "add pieces to bis" in {
      database ! impl.DatabaseBiSHandler.AddPieceToBis(Fixtures.playerEmpty.playerId, Fixtures.lootBody)
      expectMsg(timeout, 1)

      database ! impl.DatabaseBiSHandler.AddPieceToBis(Fixtures.playerEmpty.playerId, Fixtures.lootHands)
      expectMsg(timeout, 1)
    }

    "get party bis set" in {
      database ! impl.DatabaseBiSHandler.GetBiS(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyBiSCompare(party, Seq(Fixtures.lootBody, Fixtures.lootHands)) => ()
      }
    }

    "get bis set" in {
      database ! impl.DatabaseBiSHandler.GetBiS(Fixtures.playerEmpty.partyId, Some(Fixtures.playerEmpty.playerId))
      expectMsgPF(timeout) {
        case party: Seq[_] if partyBiSCompare(party, Seq(Fixtures.lootBody, Fixtures.lootHands)) => ()
      }
    }

    "remove piece from bis set" in {
      database ! impl.DatabaseBiSHandler.RemovePieceFromBiS(Fixtures.playerEmpty.playerId, Fixtures.lootBody)
      expectMsg(timeout, 1)

      database ! impl.DatabaseBiSHandler.GetBiS(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyBiSCompare(party, Seq(Fixtures.lootHands)) => ()
      }
    }

    "update piece in bis set" in {
      val newPiece = Hands(pieceType = PieceType.Savage, Job.DNC)

      database ! impl.DatabaseBiSHandler.AddPieceToBis(Fixtures.playerEmpty.playerId, newPiece)
      expectMsg(timeout, 1)

      database ! impl.DatabaseBiSHandler.GetBiS(Fixtures.playerEmpty.partyId, None)
      expectMsgPF(timeout) {
        case party: Seq[_] if partyBiSCompare(party, Seq(newPiece)) => ()
      }
    }

  }

  private def partyBiSCompare[T](party: Seq[T], bis: Seq[Piece]): Boolean =
    Compare.seqEquals(party.foldLeft(Seq.empty[Piece]){ case (acc, player) => acc ++ player.asInstanceOf[Player].bis.pieces }, bis)
}
