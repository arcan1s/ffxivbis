package me.arcanis.ffxivbis.service

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.models._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LootSelectorTest extends TestKit(ActorSystem("lootselector"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import me.arcanis.ffxivbis.utils.Converters._

  private var default: Party = Party(Some(Fixtures.partyId), Settings.config(Map.empty))
  private var dnc: Player = Player(-1, Fixtures.partyId, Job.DNC, "a nick", BiS(), Seq.empty, Some(Fixtures.link))
  private var drg: Player = Player(-1, Fixtures.partyId, Job.DRG, "another nick", BiS(), Seq.empty, Some(Fixtures.link2))
  private val timeout: FiniteDuration = 60 seconds

  override def beforeAll(): Unit = {
    val ariyala = system.actorOf(Ariyala.props)

    val dncSet = Await.result((ariyala ? Ariyala.GetBiS(Fixtures.link, Job.DNC) )(timeout).mapTo[BiS], timeout)
    dnc = dnc.withBiS(Some(dncSet))

    val drgSet = Await.result((ariyala ? Ariyala.GetBiS(Fixtures.link2, Job.DRG) )(timeout).mapTo[BiS], timeout)
    drg = drg.withBiS(Some(drgSet))

    default = default.withPlayer(dnc).withPlayer(drg)
    system.stop(ariyala)
  }

  "loot selector" must {

    "suggest loot by isRequired" in {
      toPlayerId(default.suggestLoot(Head(isTome = false, Job.AnyJob))) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot if a player already have it" in {
      val piece = Body(isTome = false, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest upgrade" in {
      val party = default.withPlayer(
        dnc.withBiS(
          Some(dnc.bis.copy(weapon = Some(Weapon(isTome = true, Job.DNC))))
        )
      )

      toPlayerId(party.suggestLoot(WeaponUpgrade)) shouldEqual Seq(dnc.playerId, drg.playerId)
    }

    "suggest loot by priority" in {
      val party = default.withPlayer(dnc.copy(priority = 2))

      toPlayerId(party.suggestLoot(Body(isTome = false, Job.AnyJob))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by bis pieces got" in {
      val party = default.withPlayer(dnc.withLoot(Head(isTome = false, Job.AnyJob)))

      toPlayerId(party.suggestLoot(Body(isTome = false, Job.AnyJob))) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by this piece got" in {
      val piece = Body(isTome = true, Job.AnyJob)
      val party = default.withPlayer(dnc.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

    "suggest loot by total piece got" in {
      val piece = Body(isTome = true, Job.AnyJob)
      val party = default
        .withPlayer(dnc.withLoot(Seq(piece, piece).map(pieceToLoot)))
        .withPlayer(drg.withLoot(piece))

      toPlayerId(party.suggestLoot(piece)) shouldEqual Seq(drg.playerId, dnc.playerId)
    }

  }

  private def toPlayerId(result: LootSelector.LootSelectorResult): Seq[PlayerId] = result.result.map(_.playerId)
}
