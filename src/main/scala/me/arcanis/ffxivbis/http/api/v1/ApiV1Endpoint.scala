package me.arcanis.ffxivbis.http.api.v1

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

class ApiV1Endpoint(storage: ActorRef, ariyala: ActorRef)(implicit timeout: Timeout) {

  private val biSEndpoint = new BiSEndpoint(storage, ariyala)
  private val lootEndpoint = new LootEndpoint(storage)
  private val playerEndpoint = new PlayerEndpoint(storage, ariyala)
  private val userEndpoint = new UserEndpoint(storage)

  def route: Route =
    biSEndpoint.route ~ lootEndpoint.route ~ playerEndpoint.route ~ userEndpoint.route
}
