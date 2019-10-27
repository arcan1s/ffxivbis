package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.server._
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.service.impl
import me.arcanis.ffxivbis.storage.Migration
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class UserEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val auth: Authorization =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint: Uri = Uri(s"/party/${Fixtures.partyId}/users")
  private val timeout: FiniteDuration = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(timeout)

  private val storage: ActorRef = system.actorOf(impl.DatabaseImpl.props)
  private val route: Route = new UserEndpoint(storage)(timeout).route

  override def testConfig: Config = Settings.withRandomDatabase

  override def beforeAll: Unit = {
    Await.result(Migration(system.settings.config), timeout)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    Settings.clearDatabase(system.settings.config)
  }

  "api v1 users endpoint" must {

    "create a party" in {
      val uri = Uri(s"/party/${Fixtures.partyId}/create")
      val entity = UserResponse.fromUser(Fixtures.userAdmin).copy(password = Fixtures.userPassword)
      println(entity)

      Put(uri, entity) ~> route ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldEqual ""
      }
    }

    "add user" in {
      val entity = UserResponse.fromUser(Fixtures.userGet).copy(password = Fixtures.userPassword2)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }
    }

    "get users" in {
      val party = Seq(Fixtures.userAdmin, Fixtures.userGet)
        .map(user => user.username -> Some(user.permission)).toMap

      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK

        val users = responseAs[Seq[UserResponse]]
        users.map(_.partyId).distinct shouldEqual Seq(Fixtures.partyId)
        users.map(user => user.username -> user.permission).toMap shouldEqual party
      }
    }

    "remove user" in {

    }

  }
}
