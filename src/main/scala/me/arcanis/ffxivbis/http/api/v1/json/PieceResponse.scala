package me.arcanis.ffxivbis.http.api.v1.json

import io.swagger.v3.oas.annotations.media.Schema
import me.arcanis.ffxivbis.models.{Job, Piece}

case class PieceResponse(
  @Schema(description = "is piece tome gear", required = true) isTome: Boolean,
  @Schema(description = "job name to which piece belong or AnyJob", required = true, example = "DNC") job: String,
  @Schema(description = "piece name", required = true, example = "body") piece: String) {
  def toPiece: Piece = Piece(piece, isTome, Job.fromString(job))
}

object PieceResponse {
  def fromPiece(piece: Piece): PieceResponse =
    PieceResponse(piece.isTome, piece.job.toString, piece.piece)
}
