package me.arcanis.ffxivbis.http.api.v1

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestKit
import com.typesafe.config.Config
import me.arcanis.ffxivbis.{Fixtures, Settings}
import me.arcanis.ffxivbis.http.api.v1.json._
import me.arcanis.ffxivbis.messages.{AddPlayer, AddUser}
import me.arcanis.ffxivbis.models.{BiS, Job}
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.{Database, PartyService}
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class BiSEndpointTest extends AnyWordSpecLike with Matchers with ScalatestRouteTest with JsonSupport {

  private val testKit = ActorTestKit(Settings.withRandomDatabase)
  override val testConfig: Config = testKit.system.settings.config

  private val auth =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint = Uri(s"/party/${Fixtures.partyId}/bis")
  private val playerId = PlayerIdResponse.fromPlayerId(Fixtures.playerEmpty.playerId)
  private val askTimeout = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(askTimeout)

  private val storage = testKit.spawn(Database())
  private val provider = testKit.spawn(BisProvider())
  private val party = testKit.spawn(PartyService(storage))
  private val route = new BiSEndpoint(party, provider)(askTimeout, testKit.scheduler).route

  override def beforeAll: Unit = {
    Await.result(Migration(testConfig), askTimeout)
    Await.result(storage.ask(AddUser(Fixtures.userAdmin, isHashedPassword = true, _))(askTimeout, testKit.scheduler), askTimeout)
    Await.result(storage.ask(AddPlayer(Fixtures.playerEmpty, _))(askTimeout, testKit.scheduler), askTimeout)
  }

  override def afterAll: Unit = {
    super.afterAll()
    Settings.clearDatabase(testConfig)
    TestKit.shutdownActorSystem(system)
    testKit.shutdownTestKit()
  }

  private def compareBiSResponse(actual: PlayerResponse, expected: PlayerResponse): Unit = {
    actual.partyId shouldEqual expected.partyId
    actual.nick shouldEqual expected.nick
    actual.job shouldEqual expected.job
    Compare.seqEquals(actual.bis.get, expected.bis.get) shouldEqual true
    actual.link shouldEqual expected.link
    actual.priority shouldEqual expected.priority
  }

  "api v1 bis endpoint" must {

    "create best in slot set from ariyala" in {
      val entity = PlayerBiSLinkResponse(Fixtures.link, playerId)

      Put(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldEqual ""
      }
    }

    "return best in slot set" in {
      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "remove item from best in slot set" in {
      val piece = PieceResponse.fromPiece(Fixtures.lootBody)
      val entity = PieceActionResponse(ApiAction.remove, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val bis = BiS(Fixtures.bis.pieces.filterNot(_ == Fixtures.lootBody))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "add item to best in slot set" in {
      val piece = PieceResponse.fromPiece(Fixtures.lootBody)
      val entity = PieceActionResponse(ApiAction.add, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "do not allow to add same item to best in slot set" in {
      val piece = PieceResponse.fromPiece(Fixtures.lootBody.withJob(Job.DNC))
      val entity = PieceActionResponse(ApiAction.add, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "allow to add item with another type to best in slot set" in {
      val piece = PieceResponse.fromPiece(Fixtures.lootBodyCrafted.withJob(Job.DNC))
      val entity = PieceActionResponse(ApiAction.add, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val bis = Fixtures.bis.withPiece(piece.toPiece)
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "remove only specific item from best in slot set" in {
      val piece = PieceResponse.fromPiece(Fixtures.lootBodyCrafted.withJob(Job.DNC))
      val entity = PieceActionResponse(ApiAction.remove, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

    "totaly replace player bis" in {
      // add random item first
      val piece = PieceResponse.fromPiece(Fixtures.lootBodyCrafted.withJob(Job.DNC))
      val entity = PieceActionResponse(ApiAction.add, piece, playerId, None)

      Post(endpoint, entity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
        responseAs[String] shouldEqual ""
      }

      val bisEntity = PlayerBiSLinkResponse(Fixtures.link, playerId)

      Put(endpoint, bisEntity).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldEqual ""
      }

      val uri = endpoint.withQuery(Uri.Query(Map("nick" -> playerId.nick, "job" -> playerId.job)))
      val response = PlayerResponse.fromPlayer(Fixtures.playerEmpty.withBiS(Some(Fixtures.bis)))

      Get(uri).withHeaders(auth) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val actual = responseAs[Seq[PlayerResponse]]
        actual.length shouldEqual 1
        actual.foreach(compareBiSResponse(_, response))
      }
    }

  }
}
