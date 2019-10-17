/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.security.SecurityScheme

object Swagger extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(
    classOf[api.v1.BiSEndpoint], classOf[api.v1.LootEndpoint],
    classOf[api.v1.PlayerEndpoint], classOf[api.v1.UserEndpoint]
  )

  override val info: Info = Info()

  private val basicAuth = new SecurityScheme()
    .description("basic http auth")
    .`type`(SecurityScheme.Type.HTTP)
    .in(SecurityScheme.In.HEADER)
    .scheme("bearer")
  override def securitySchemes: Map[String, SecurityScheme] = Map("basic auth" -> basicAuth)

  override val unwantedDefinitions: Seq[String] =
    Seq("Function1", "Function1RequestContextFutureRouteResult")
}
