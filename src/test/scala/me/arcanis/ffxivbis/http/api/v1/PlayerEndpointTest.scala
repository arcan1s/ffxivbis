package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.{PartyService, impl}
import me.arcanis.ffxivbis.storage.Migration
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class PlayerEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val auth: Authorization =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint: Uri = Uri(s"/party/${Fixtures.partyId}")
  private val playerId = PlayerIdResponse.fromPlayerId(Fixtures.playerEmpty.playerId)
  private val timeout: FiniteDuration = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(timeout)

  private val storage: ActorRef = system.actorOf(impl.DatabaseImpl.props)
  private val provider: ActorRef = system.actorOf(BisProvider.props)
  private val party: ActorRef = system.actorOf(PartyService.props(storage))
  private val route: Route = new PlayerEndpoint(party, provider)(timeout).route

  override def testConfig: Config = Settings.withRandomDatabase

  override def beforeAll: Unit = {
    Await.result(Migration(system.settings.config), timeout)
    Await.result((storage ? impl.DatabaseUserHandler.AddUser(Fixtures.userAdmin, isHashedPassword = true))(timeout).mapTo[Int], timeout)
    Await.result((storage ? impl.DatabasePartyHandler.AddPlayer(Fixtures.playerEmpty))(timeout).mapTo[Int], timeout)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    Settings.clearDatabase(system.settings.config)
  }

  "api v1 player endpoint" must {

    "get users" in {
      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = Seq(PlayerResponse.fromPlayer(Fixtures.playerEmpty))

      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[PlayerResponse]] shouldEqual response
      }
    }

  }
}
