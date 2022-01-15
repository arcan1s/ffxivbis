/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import akka.http.scaladsl.model.headers.{`User-Agent`, Authorization, BasicHttpCredentials, Referer}
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{extractClientIP, extractRequestContext, mapResponse, optionalHeaderValueByType}
import com.typesafe.scalalogging.Logger

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

trait HttpLog {

  private val httpLogger = Logger("http")

  def withHttpLog: Directive0 =
    extractRequestContext.flatMap { context =>
      val request = s"${context.request.method.name()} ${context.request.uri.path}"

      extractClientIP.flatMap { maybeRemoteAddr =>
        val remoteAddr = maybeRemoteAddr.toIP.getOrElse("-")

        optionalHeaderValueByType(Referer).flatMap { maybeReferer =>
          val referer = maybeReferer.map(_.uri).getOrElse("-")

          optionalHeaderValueByType(`User-Agent`).flatMap { maybeUserAgent =>
            val userAgent = maybeUserAgent.map(_.products.map(_.toString()).mkString(" ")).getOrElse("-")

            optionalHeaderValueByType(Authorization).flatMap { maybeAuth =>
              val remoteUser = maybeAuth
                .map(_.credentials)
                .collect { case BasicHttpCredentials(username, _) =>
                  username
                }
                .getOrElse("-")

              val start = Instant.now.toEpochMilli
              val timeLocal = HttpLog.httpLogDatetimeFormatter.format(Instant.now)

              mapResponse { response =>
                val time = (Instant.now.toEpochMilli - start) / 1000.0

                val status = response.status.intValue()
                val bytesSent = response.entity.getContentLengthOption.getAsLong

                httpLogger.debug(
                  s"""$remoteAddr - $remoteUser [$timeLocal] "$request" $status $bytesSent "$referer" "$userAgent" $time"""
                )
                response
              }
            }
          }
        }
      }
    }

}

object HttpLog {

  val httpLogDatetimeFormatter: DateTimeFormatter =
    DateTimeFormatter
      .ofPattern("dd/MMM/uuuu:HH:mm:ss xx  ")
      .withLocale(Locale.UK)
      .withZone(ZoneId.systemDefault())
}
