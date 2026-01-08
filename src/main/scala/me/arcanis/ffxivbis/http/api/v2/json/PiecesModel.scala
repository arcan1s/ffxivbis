/*
 * Copyright (c) 2021-2026 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.http.api.v2.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.http.api.v1.json.PieceModel
import me.arcanis.ffxivbis.models.Piece

case class PiecesModel(
  @Schema(description = "pieces list", required = true) pieces: Seq[PieceModel],
) {

  def toPiece: Seq[Piece] = pieces.map(_.toPiece)
}

object PiecesModel {

  def fromPiece(pieces: Seq[Piece]): PiecesModel = PiecesModel(pieces.map(PieceModel.fromPiece))
}
