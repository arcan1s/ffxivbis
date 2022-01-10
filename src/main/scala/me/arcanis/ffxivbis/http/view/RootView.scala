/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.view

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import me.arcanis.ffxivbis.http.{Authorization, AuthorizationProvider}

class RootView(override val auth: AuthorizationProvider) extends Authorization {

  def route: Route = getBiS ~ getIndex ~ getLoot ~ getParty ~ getUsers

  def getBiS: Route =
    path("party" / Segment / "bis") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { user =>
          respondWithHeaders(
            RawHeader("X-Party-Id", partyId),
            RawHeader("X-User-Permission", user.permission.toString)
          ) {
            getFromResource("html/bis.html")
          }
        }
      }
    }

  def getIndex: Route =
    pathEndOrSingleSlash {
      getFromResource("html/index.html")
    }

  def getLoot: Route =
    path("party" / Segment / "loot") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { user =>
          respondWithHeaders(
            RawHeader("X-Party-Id", partyId),
            RawHeader("X-User-Permission", user.permission.toString)
          ) {
            getFromResource("html/loot.html")
          }
        }
      }
    }

  def getParty: Route =
    path("party" / Segment) { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authGet(partyId)) { user =>
          respondWithHeaders(
            RawHeader("X-Party-Id", partyId),
            RawHeader("X-User-Permission", user.permission.toString)
          ) {
            getFromResource("html/party.html")
          }
        }
      }
    }

  def getUsers: Route =
    path("party" / Segment / "users") { partyId: String =>
      extractExecutionContext { implicit executionContext =>
        authenticateBasicBCrypt(s"party $partyId", authAdmin(partyId)) { user =>
          respondWithHeaders(
            RawHeader("X-Party-Id", partyId),
            RawHeader("X-User-Permission", user.permission.toString)
          ) {
            getFromResource("html/users.html")
          }
        }
      }
    }
}
