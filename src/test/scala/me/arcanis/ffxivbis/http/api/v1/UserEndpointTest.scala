package me.arcanis.ffxivbis.http.api.v1

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.database.Database
import me.arcanis.ffxivbis.storage.Migration
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class UserEndpointTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest with JsonSupport {

  private val testKit = ActorTestKit(Settings.withRandomDatabase)
  override val testConfig: Config = testKit.system.settings.config

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private def endpoint = Uri(s"/party/$partyId/users")
  private val askTimeout = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(askTimeout)

  private var partyId = Fixtures.partyId
  private val storage = testKit.spawn(Database())
  private val party = testKit.spawn(PartyService(storage))
  private val route = new UserEndpoint(party)(askTimeout, testKit.scheduler).route

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration(testConfig)
  }

  override def afterAll(): Unit = {
    Settings.clearDatabase(testConfig)
    TestKit.shutdownActorSystem(system)
    testKit.shutdownTestKit()
    super.afterAll()
  }

  "api v1 users endpoint" must {

    "create a party" in {
      val uri = Uri(s"/party")
      val entity = UserResponse.fromUser(Fixtures.userAdmin).copy(password = Fixtures.userPassword)

      Put(uri, entity) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        partyId = responseAs[PartyIdResponse].partyId
      }
    }

    "add user" in {
      val entity = UserResponse.fromUser(Fixtures.userGet).copy(partyId = partyId, password = Fixtures.userPassword2)

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
        users.map(_.partyId).distinct shouldEqual Seq(partyId)
        users.map(user => user.username -> user.permission).toMap shouldEqual party
      }
    }

    "remove user" in {
      val entity = UserResponse.fromUser(Fixtures.userGet).copy(partyId = partyId)

      Delete(endpoint.toString + s"/${entity.username}").withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
      }

      val party = Seq(Fixtures.userAdmin)
        .map(user => user.username -> Some(user.permission)).toMap

      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK

        val users = responseAs[Seq[UserResponse]]
        users.map(_.partyId).distinct shouldEqual Seq(partyId)
        users.map(user => user.username -> user.permission).toMap shouldEqual party
      }
    }

  }
}
