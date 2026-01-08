/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v2

import me.arcanis.ffxivbis.http.api.v1.{HttpHandler => HttpHandlerV1}
import me.arcanis.ffxivbis.http.api.v2.json.JsonSupport

trait HttpHandler extends HttpHandlerV1 { this: JsonSupport => }
