package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema

case class ErrorResponse(
  @Schema(description = "error message", required = true) message: String)
