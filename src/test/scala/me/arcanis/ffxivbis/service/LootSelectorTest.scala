package me.arcanis.ffxivbis.service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import me.arcanis.ffxivbis.messages.BiSProviderMessage.DownloadBiS
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LootSelectorTest extends AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  import me.arcanis.ffxivbis.utils.Converters._

  private var default: Party = Party(PartyDescription.empty(Fixtures.partyId), Settings.config(Map.empty), Map.empty, Seq.empty, Seq.empty)
  private var dnc: Player = Player(-1, Fixtures.partyId, Job.DNC, "a nick", BiS.empty, Seq.empty, Some(Fixtures.link))
  private var drg: Player = Player(-1, Fixtures.partyId, Job.DRG, "another nick", BiS.empty, Seq.empty, Some(Fixtures.link2))
  private val timeout: FiniteDuration = 60 seconds

  override def beforeAll(): Unit = {
    super.beforeAll()
    val testKit = ActorTestKit(Settings.withRandomDatabase)
    val provider = testKit.spawn(BisProvider())

    val dncSet = Await.result(provider.ask(DownloadBiS(Fixtures.link, Job.DNC, _))(timeout, testKit.scheduler), timeout)
    dnc = dnc.withBiS(Some(dncSet))

    val drgSet = Await.result(provider.ask(DownloadBiS(Fixtures.link2, Job.DRG, _))(timeout, testKit.scheduler), timeout)
    drg = drg.withBiS(Some(drgSet))

    default = default.withPlayer(dnc).withPlayer(drg)

    testKit.stop(provider)
    testKit.shutdownTestKit()
  }

  "loot selector" must {

    "suggest loot by isRequired" in {
      toPlayerId(default.suggestLoot(Seq(Piece.Head(pieceType = PieceType.Savage, Job.AnyJob)))) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot if a player already have it" in {
      val piece = Piece.Body(pieceType = PieceType.Savage, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(Seq(piece))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest upgrade" in {
      val party = default.withPlayer(
        dnc.withBiS(
          Some(dnc.bis.withPiece(Piece.Weapon(pieceType = PieceType.Tome, Job.DNC)))
        )
      )

      toPlayerId(party.suggestLoot(Seq(Piece.WeaponUpgrade))) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot by priority" in {
      val party = default.withPlayer(dnc.copy(priority = 2))

      toPlayerId(party.suggestLoot(Seq(Piece.Body(pieceType = PieceType.Savage, Job.AnyJob)))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by bis pieces got" in {
      val party = default.withPlayer(dnc.withLoot(Piece.Head(pieceType = PieceType.Savage, Job.AnyJob)))

      toPlayerId(party.suggestLoot(Seq(Piece.Body(pieceType = PieceType.Savage, Job.AnyJob)))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by this piece got" in {
      val piece = Piece.Body(pieceType = PieceType.Tome, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(Seq(piece))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by total piece got" in {
      val piece = Piece.Body(pieceType = PieceType.Tome, Job.AnyJob)
      val party = default
        .withPlayer(dnc.withLoot(Seq(piece, piece).map(pieceToLoot)))
        .withPlayer(drg.withLoot(piece))

      toPlayerId(party.suggestLoot(Seq(piece))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

  }

  private def toPlayerId(result: LootSelector.LootSelectorResult): Seq[PlayerId] = result.result.map(_.playerId)
}
