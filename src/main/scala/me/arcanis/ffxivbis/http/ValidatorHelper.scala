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
