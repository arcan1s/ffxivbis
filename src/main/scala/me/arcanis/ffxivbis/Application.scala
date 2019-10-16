package me.arcanis.ffxivbis

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import me.arcanis.ffxivbis.http.RootEndpoint
import me.arcanis.ffxivbis.service.Ariyala
import me.arcanis.ffxivbis.service.impl.DatabaseImpl
import me.arcanis.ffxivbis.storage.Migration

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class Application extends Actor with StrictLogging {
  implicit private val executionContext: ExecutionContext = context.system.dispatcher
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val config = context.system.settings.config
  private val host = config.getString("me.arcanis.ffxivbis.web.host")
  private val port = config.getInt("me.arcanis.ffxivbis.web.port")

  override def receive: Receive = Actor.emptyBehavior

  Migration(config).onComplete {
    case Success(_) =>
      val ariyala = context.system.actorOf(Ariyala.props, "ariyala")
      val storage = context.system.actorOf(DatabaseImpl.props, "storage")
      val http = new RootEndpoint(context.system, storage, ariyala)

      logger.info(s"start server at $host:$port")
      val bind = Http()(context.system).bindAndHandle(http.route, host, port)
      Await.result(context.system.whenTerminated, Duration.Inf)
      bind.foreach(_.unbind())

    case Failure(exception) => throw exception
  }
}

object Application {
  def props: Props = Props(new Application)
}
