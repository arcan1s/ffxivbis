package me.arcanis.ffxivbis.http.api.v1

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.messages.{AddPlayer, AddUser}
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.database.{Database, Migration}
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class PlayerEndpointTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest with JsonSupport {

  private val testKit = ActorTestKit(Settings.withRandomDatabase)
  override val testConfig: Config = testKit.system.settings.config

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint = Uri(s"/party/${Fixtures.partyId}")
  private val askTimeout = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(askTimeout)

  private val storage = testKit.spawn(Database())
  private val provider = testKit.spawn(BisProvider())
  private val party = testKit.spawn(PartyService(storage))
  private val route = new PlayerEndpoint(party, provider, Fixtures.authProvider)(askTimeout, testKit.scheduler).routes

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration(testConfig)
    Await.result(storage.ask(AddUser(Fixtures.userAdmin, isHashedPassword = true, _))(askTimeout, testKit.scheduler), askTimeout)
    Await.result(storage.ask(AddPlayer(Fixtures.playerEmpty, _))(askTimeout, testKit.scheduler), askTimeout)
  }

  override def afterAll(): Unit = {
    Settings.clearDatabase(testConfig)
    TestKit.shutdownActorSystem(system)
    testKit.shutdownTestKit()
    super.afterAll()
  }

  "api v1 player endpoint" must {

    "get users" in {
      val response = Seq(PlayerModel.fromPlayer(Fixtures.playerEmpty))

      Get(endpoint).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[PlayerModel]] shouldEqual response
      }
    }

  }
}
