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
import me.arcanis.ffxivbis.models.{Job, Piece, PieceType}

case class PieceResponse(
  @Schema(description = "piece type", required = true) pieceType: String,
  @Schema(description = "job name to which piece belong or AnyJob", required = true, example = "DNC") job: String,
  @Schema(description = "piece name", required = true, example = "body") piece: String) {

  def toPiece: Piece = Piece(piece, PieceType.withName(pieceType), Job.withName(job))
}

object PieceResponse {

  def fromPiece(piece: Piece): PieceResponse =
    PieceResponse(piece.pieceType.toString, piece.job.toString, piece.piece)
}
