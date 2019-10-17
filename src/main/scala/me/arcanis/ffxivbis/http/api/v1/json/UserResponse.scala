/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.{Permission, User}

case class UserResponse(
  @Schema(description = "unique party ID", required = true, example = "abcdefgh") partyId: String,
  @Schema(description = "username to login to party", required = true, example = "siuan") username: String,
  @Schema(description = "password to login to party", required = true, example = "pa55w0rd") password: String,
  @Schema(description = "user permission", defaultValue = "get", allowableValues = Array("get", "post", "admin")) permission: Option[Permission.Value] = None) {
  def toUser: User =
    User(partyId, username, password, permission.getOrElse(Permission.get))
}

object UserResponse {
  def fromUser(user: User): UserResponse =
    UserResponse(user.partyId, user.username, "", Some(user.permission))
}
