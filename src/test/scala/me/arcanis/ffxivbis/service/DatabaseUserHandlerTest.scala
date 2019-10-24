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

class DatabaseUserHandlerTest
  extends TestKit(ActorSystem("database-user-handler", Settings.withRandomDatabase))
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

  "database user handler actor" must {

    "add user" in {
      database ! impl.DatabaseUserHandler.AddUser(Fixtures.userAdmin, isHashedPassword = true)
      expectMsg(timeout, 1)
    }

    "get user" in {
      database ! impl.DatabaseUserHandler.GetUser(Fixtures.partyId, Fixtures.userAdmin.username)
      expectMsg(timeout, Some(Fixtures.userAdmin))
    }

    "get users" in {
      database ! impl.DatabaseUserHandler.AddUser(Fixtures.userGet, isHashedPassword = true)
      expectMsg(timeout, 1)

      database ! impl.DatabaseUserHandler.GetUsers(Fixtures.partyId)
      expectMsgPF(timeout) {
        case u: Seq[_] if Compare.seqEquals(u, Fixtures.users) => ()
      }
    }

    "update user" in {
      val newUser= Fixtures.userGet.copy(password = Fixtures.userPassword2).withHashedPassword
      val newUserSet = Seq(newUser, Fixtures.userAdmin)

      database ! impl.DatabaseUserHandler.AddUser(newUser, isHashedPassword = true)
      expectMsg(timeout, 1)

      database ! impl.DatabaseUserHandler.GetUser(Fixtures.partyId, newUser.username)
      expectMsg(timeout, Some(newUser))

      database ! impl.DatabaseUserHandler.GetUsers(Fixtures.partyId)
      expectMsgPF(timeout) {
        case u: Seq[_] if Compare.seqEquals(u, newUserSet) => ()
      }
    }

    "remove user" in {
      database ! impl.DatabaseUserHandler.DeleteUser(Fixtures.partyId, Fixtures.userGet.username)
      expectMsg(timeout, 1)

      database ! impl.DatabaseUserHandler.GetUser(Fixtures.partyId, Fixtures.userGet.username)
      expectMsg(timeout, None)
    }
  }
}
