package me.arcanis.ffxivbis.service

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.models._
import me.arcanis.ffxivbis.service.bis.BisProvider
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LootSelectorTest extends TestKit(ActorSystem("lootselector"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import me.arcanis.ffxivbis.utils.Converters._

  private var default: Party = Party(PartyDescription.empty(Fixtures.partyId), Settings.config(Map.empty), Map.empty, Seq.empty, Seq.empty)
  private var dnc: Player = Player(-1, Fixtures.partyId, Job.DNC, "a nick", BiS(), Seq.empty, Some(Fixtures.link))
  private var drg: Player = Player(-1, Fixtures.partyId, Job.DRG, "another nick", BiS(), Seq.empty, Some(Fixtures.link2))
  private val timeout: FiniteDuration = 60 seconds

  override def beforeAll(): Unit = {
    val provider = system.actorOf(BisProvider.props(false))

    val dncSet = Await.result((provider ? BisProvider.GetBiS(Fixtures.link, Job.DNC) )(timeout).mapTo[BiS], timeout)
    dnc = dnc.withBiS(Some(dncSet))

    val drgSet = Await.result((provider ? BisProvider.GetBiS(Fixtures.link2, Job.DRG) )(timeout).mapTo[BiS], timeout)
    drg = drg.withBiS(Some(drgSet))

    default = default.withPlayer(dnc).withPlayer(drg)
    system.stop(provider)
  }

  "loot selector" must {

    "suggest loot by isRequired" in {
      toPlayerId(default.suggestLoot(Head(pieceType = PieceType.Savage, Job.AnyJob))) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot if a player already have it" in {
      val piece = Body(pieceType = PieceType.Savage, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest upgrade" in {
      val party = default.withPlayer(
        dnc.withBiS(
          Some(dnc.bis.copy(weapon = Some(Weapon(pieceType = PieceType.Tome, Job.DNC))))
        )
      )

      toPlayerId(party.suggestLoot(WeaponUpgrade)) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot by priority" in {
      val party = default.withPlayer(dnc.copy(priority = 2))

      toPlayerId(party.suggestLoot(Body(pieceType = PieceType.Savage, Job.AnyJob))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by bis pieces got" in {
      val party = default.withPlayer(dnc.withLoot(Head(pieceType = PieceType.Savage, Job.AnyJob)))

      toPlayerId(party.suggestLoot(Body(pieceType = PieceType.Savage, Job.AnyJob))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by this piece got" in {
      val piece = Body(pieceType = PieceType.Tome, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by total piece got" in {
      val piece = Body(pieceType = PieceType.Tome, Job.AnyJob)
      val party = default
        .withPlayer(dnc.withLoot(Seq(piece, piece).map(pieceToLoot)))
        .withPlayer(drg.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

  }

  private def toPlayerId(result: LootSelector.LootSelectorResult): Seq[PlayerId] = result.result.map(_.playerId)
}
