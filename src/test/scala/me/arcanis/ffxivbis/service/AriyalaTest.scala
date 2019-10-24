package me.arcanis.ffxivbis.service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import me.arcanis.ffxivbis.models.{Fixtures, Job}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class AriyalaTest extends TestKit(ActorSystem("ariyala"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  private val timeout: FiniteDuration = 60 seconds

  override def afterAll: Unit = TestKit.shutdownActorSystem(system)

  "ariyala actor" must {

    "get best in slot set" in {
      val ariyala = system.actorOf(Ariyala.props)
      ariyala ! Ariyala.GetBiS(Fixtures.link, Job.DNC)
      expectMsg(timeout, Fixtures.bis)
    }

  }
}
