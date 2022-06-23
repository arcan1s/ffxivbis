package me.arcanis.ffxivbis.http.api.v1.json

import me.arcanis.ffxivbis.http.ValidatorHelper

trait Validator extends ValidatorHelper {

  def stringMatchError(what: String): String =
    s"$what must contain only letters or digits or one of (${ValidatorHelper.VALID_CHARACTERS.mkString(", ")})"
}
