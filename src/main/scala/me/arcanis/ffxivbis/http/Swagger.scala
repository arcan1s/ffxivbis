/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.{Info, License}
import com.typesafe.config.Config
import io.swagger.v3.oas.models.security.SecurityScheme

import scala.io.Source

class Swagger(config: Config) extends SwaggerHttpService {

  override val apiClasses: Set[Class[_]] = Set(
    classOf[api.v1.BiSEndpoint],
    classOf[api.v1.LootEndpoint],
    classOf[api.v1.PartyEndpoint],
    classOf[api.v1.PlayerEndpoint],
    classOf[api.v1.StatusEndpoint],
    classOf[api.v1.TypesEndpoint],
    classOf[api.v1.UserEndpoint]
  )

  override val info: Info = Info(
    description = Source.fromResource("swagger-info/description.md").mkString,
    version = getClass.getPackage.getImplementationVersion,
    title = "FFXIV static loot tracker",
    license = Some(License("BSD", "https://raw.githubusercontent.com/arcan1s/ffxivbis/master/LICENSE"))
  )

  override val host: String =
    if (config.hasPath("me.arcanis.ffxivbis.web.hostname")) config.getString("me.arcanis.ffxivbis.web.hostname")
    else s"${config.getString("me.arcanis.ffxivbis.web.host")}:${config.getInt("me.arcanis.ffxivbis.web.port")}"

  private val basicAuth = new SecurityScheme()
    .description("basic http auth")
    .`type`(SecurityScheme.Type.HTTP)
    .in(SecurityScheme.In.HEADER)
    .scheme("bearer")
  override val securitySchemes: Map[String, SecurityScheme] = Map("basic auth" -> basicAuth)

  override val unwantedDefinitions: Seq[String] =
    Seq("Function1", "Function1RequestContextFutureRouteResult")
}
