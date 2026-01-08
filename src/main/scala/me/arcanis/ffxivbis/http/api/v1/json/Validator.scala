/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v1.json

import me.arcanis.ffxivbis.http.ValidatorHelper

trait Validator extends ValidatorHelper {

  def stringMatchError(what: String): String =
    s"$what must contain only letters or digits or one of (${ValidatorHelper.VALID_CHARACTERS.mkString(", ")})"
}
