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
import me.arcanis.ffxivbis.models.{BiS, Body, Job, PieceType}
import me.arcanis.ffxivbis.service.bis.BisProvider
import me.arcanis.ffxivbis.service.{PartyService, impl}
import me.arcanis.ffxivbis.storage.Migration
import me.arcanis.ffxivbis.utils.Compare
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class BiSEndpointTest extends WordSpec
  with Matchers with ScalatestRouteTest with JsonSupport {

  private val auth: Authorization =
    Authorization(BasicHttpCredentials(Fixtures.userAdmin.username, Fixtures.userPassword))
  private val endpoint: Uri = Uri(s"/party/${Fixtures.partyId}/bis")
  private val playerId = PlayerIdResponse.fromPlayerId(Fixtures.playerEmpty.playerId)
  private val timeout: FiniteDuration = 60 seconds
  implicit private val routeTimeout: RouteTestTimeout = RouteTestTimeout(timeout)

  private val storage: ActorRef = system.actorOf(impl.DatabaseImpl.props)
  private val provider: ActorRef = system.actorOf(BisProvider.props)
  private val party: ActorRef = system.actorOf(PartyService.props(storage))
  private val route: Route = new BiSEndpoint(party, provider)(timeout).route

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
