package me.arcanis.ffxivbis.service

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import me.arcanis.ffxivbis.messages.{AddUser, DeleteUser, GetUser, GetUsers}
import me.arcanis.ffxivbis.models.User
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabaseUserHandlerTest extends ScalaTestWithActorTestKit(Settings.withRandomDatabase)
  with AnyWordSpecLike {

  private val database = testKit.spawn(Database())
  private val askTimeout = 60 seconds

  override def beforeAll: Unit = {
    Await.result(Migration(testKit.system.settings.config), askTimeout)
  }

  override def afterAll: Unit = {
    super.afterAll()
    Settings.clearDatabase(testKit.system.settings.config)
  }

  "database user handler actor" must {

    "add user" in {
      val probe = testKit.createTestProbe[Unit]()
      database ! AddUser(Fixtures.userAdmin, isHashedPassword = true, probe.ref)
      probe.expectMessage(askTimeout, ())
    }

    "get user" in {
      val probe = testKit.createTestProbe[Option[User]]()
      database ! GetUser(Fixtures.partyId, Fixtures.userAdmin.username, probe.ref)
      probe.expectMessage(askTimeout, Some(Fixtures.userAdmin))
    }

    "get users" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! AddUser(Fixtures.userGet, isHashedPassword = true, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Seq[User]]()
      database ! GetUsers(Fixtures.partyId, probe.ref)

      val users = probe.expectMessageType[Seq[User]]
      Compare.seqEquals(users, Fixtures.users) shouldEqual true
    }

    "update user" in {
      val newUser= Fixtures.userGet.copy(password = Fixtures.userPassword2).withHashedPassword
      val newUserSet = Seq(newUser, Fixtures.userAdmin)

      val updateProbe = testKit.createTestProbe[Unit]()
      database ! AddUser(newUser, isHashedPassword = true, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Option[User]]()
      database ! GetUser(Fixtures.partyId, newUser.username, probe.ref)
      probe.expectMessage(askTimeout, Some(newUser))

      val partyProbe = testKit.createTestProbe[Seq[User]]()
      database ! GetUsers(Fixtures.partyId, partyProbe.ref)

      val users = partyProbe.expectMessageType[Seq[User]]
      Compare.seqEquals(users, newUserSet) shouldEqual true
    }

    "remove user" in {
      val updateProbe = testKit.createTestProbe[Unit]()
      database ! DeleteUser(Fixtures.partyId, Fixtures.userGet.username, updateProbe.ref)
      updateProbe.expectMessage(askTimeout, ())

      val probe = testKit.createTestProbe[Option[User]]()
      database ! GetUser(Fixtures.partyId, Fixtures.userGet.username, probe.ref)
      probe.expectMessage(askTimeout, None)
    }
  }
}
