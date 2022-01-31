package me.arcanis.ffxivbis.http.api.v1

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.messages.DatabaseMessage.{AddPlayer, AddUser}
import me.arcanis.ffxivbis.service.PartyService
import me.arcanis.ffxivbis.service.database.{Database, Migration}
import me.arcanis.ffxivbis.{Fixtures, Settings}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LootEndpointTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest with JsonSupport {

  private val testKit = ActorTestKit(Settings.withRandomDatabase)
  override val testConfig: Config = testKit.system.settings.config

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint = Uri(s"/party/${Fixtures.partyId}/loot")
  private val playerId = PlayerIdModel.fromPlayerId(Fixtures.playerEmpty.playerId)
  private val askTimeout = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(askTimeout)

  private val storage = testKit.spawn(Database())
  private val party = testKit.spawn(PartyService(storage))
  private val route = new LootEndpoint(party, Fixtures.authProvider)(askTimeout, testKit.scheduler).routes

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

  "api v1 loot endpoint" must {

    "add item to loot" in {
      val piece = PieceModel.fromPiece(Fixtures.lootBody)
      val entity = PieceActionModel(ApiAction.add, piece, playerId, Some(false))

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }
    }

    "return looted items" in {
      import me.arcanis.ffxivbis.utils.Converters._

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = Seq(PlayerModel.fromPlayer(Fixtures.playerEmpty.withLoot(Fixtures.lootBody)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val withEmptyTimestamp = responseAs[Seq[PlayerModel]].map { player =>
          player.copy(loot = player.loot.map(_.map(_.copy(timestamp = Instant.ofEpochMilli(0)))))
        }
        withEmptyTimestamp shouldEqual response
      }
    }

    "remove item from loot" in {
      val piece = PieceModel.fromPiece(Fixtures.lootBody)
      val entity = PieceActionModel(ApiAction.remove, piece, playerId, Some(false))

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = Seq(PlayerModel.fromPlayer(Fixtures.playerEmpty))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[PlayerModel]] shouldEqual response
      }
    }

    "suggest loot" in {
      val entity = PieceModel.fromPiece(Fixtures.lootBody)
      val response = Seq(Fixtures.playerEmpty.withCounters(Some(Fixtures.lootBody))).map(PlayerIdWithCountersModel.fromPlayerId)

      Put(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[PlayerIdWithCountersModel]] shouldEqual response
      }
    }

  }
}
