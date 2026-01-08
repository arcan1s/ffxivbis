/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http

import scala.collection.immutable.HashSet

trait ValidatorHelper {

  def isValidString(string: String): Boolean = string.nonEmpty && string.forall(isValidSymbol)

  def isValidSymbol(char: Char): Boolean =
    char.isLetterOrDigit || ValidatorHelper.VALID_CHARACTERS.contains(char)
}

object ValidatorHelper {

  final val VALID_CHARACTERS = HashSet.from("!@#$%^&*()-_=+;:',./?| ")
}
