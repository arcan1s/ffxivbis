/*
 * Copyright (c) 2019 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

object Job {
  sealed trait Job

  case object AnyJob extends Job {
    override def equals(obj: Any): Boolean = obj match {
      case Job => true
      case _ => false
    }
  }

  case object PLD extends Job
  case object WAR extends Job
  case object DRK extends Job
  case object GNB extends Job

  case object WHM extends Job
  case object SCH extends Job
  case object AST extends Job

  case object MNK extends Job
  case object DRG extends Job
  case object NIN extends Job
  case object SAM extends Job

  case object BRD extends Job
  case object MCH extends Job
  case object DNC extends Job

  case object BLM extends Job
  case object SMN extends Job
  case object RDM extends Job

  def groupAccessoriesDex: Seq[Job.Job] = groupRanges :+ NIN
  def groupAccessoriesStr: Seq[Job.Job] = groupMnk :+ DRG
  def groupAll: Seq[Job.Job] = groupCasters ++ groupHealers ++ groupRanges ++ groupTanks
  def groupCasters: Seq[Job.Job] = Seq(BLM, SMN, RDM)
  def groupHealers: Seq[Job.Job] = Seq(WHM, SCH, AST)
  def groupMnk: Seq[Job.Job] = Seq(MNK, SAM)
  def groupRanges: Seq[Job.Job] = Seq(BRD, MCH, DNC)
  def groupTanks: Seq[Job.Job] = Seq(PLD, WAR, DRK, GNB)

  def groupFull: Seq[Seq[Job.Job]] = Seq(groupCasters, groupHealers, groupMnk, groupRanges, groupTanks)
  def groupRight: Seq[Seq[Job.Job]] = Seq(groupAccessoriesDex, groupAccessoriesStr)

  def fromString(job: String): Job.Job = groupAll.find(_.toString == job.toUpperCase).orNull

  def hasSameLoot(left: Job, right: Job, piece: Piece): Boolean = {
    def isAccessory(piece: Piece): Boolean = piece match {
      case _: PieceAccessory => true
      case _ => false
    }
    def isWeapon(piece: Piece): Boolean = piece match {
      case _: PieceWeapon => true
      case _ => false
    }

    if (left == right) true
    else if (isWeapon(piece)) false
    else if (groupFull.exists(group => group.contains(left) && group.contains(right))) true
    else if (isAccessory(piece) && groupRight.exists(group => group.contains(left) && group.contains(right))) true
    else false
  }
}
