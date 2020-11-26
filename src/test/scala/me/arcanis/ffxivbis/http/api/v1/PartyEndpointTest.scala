package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.server._
import akka.testkit.TestKit
import akka.pattern.ask
import com.typesafe.config.Config
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.models.PartyDescription
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.impl
import me.arcanis.ffxivbis.storage.Migration
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class PartyEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val auth: Authorization =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint: Uri = Uri(s"/party/${Fixtures.partyId}/description")
  private val timeout: FiniteDuration = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(timeout)

  private val storage: ActorRef = system.actorOf(impl.DatabaseImpl.props)
  private val provider: ActorRef = system.actorOf(BisProvider.props)
  private val route: Route = new PartyEndpoint(storage, provider)(timeout).route

  override def testConfig: Config = Settings.withRandomDatabase

  override def beforeAll: Unit = {
    Await.result(Migration(system.settings.config), timeout)
    Await.result((storage ? impl.DatabaseUserHandler.AddUser(Fixtures.userAdmin, isHashedPassword = true))(timeout).mapTo[Int], timeout)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    Settings.clearDatabase(system.settings.config)
  }

  "api v1 party endpoint" must {

    "get empty party description" in {
      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[PartyDescriptionResponse].toDescription shouldEqual PartyDescription.empty(Fixtures.partyId)
      }
    }

    "update party description" in {
      val entity = PartyDescriptionResponse(Fixtures.partyId, Some("random party name"))

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
      }

      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[PartyDescriptionResponse].toDescription shouldEqual entity.toDescription
      }
    }

  }
}
