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
  sealed trait RightSide
  object AccessoriesDex extends RightSide
  object AccessoriesInt extends RightSide
  object AccessoriesMnd extends RightSide
  object AccessoriesStr extends RightSide
  object AccessoriesVit extends RightSide

  sealed trait LeftSide
  object BodyCasters extends LeftSide
  object BodyDrgs extends LeftSide
  object BodyHealers extends LeftSide
  object BodyMnks extends LeftSide
  object BodyNins extends LeftSide
  object BodyTanks extends LeftSide
  object BodyRanges extends LeftSide

  sealed trait Job {
    def leftSide: LeftSide
    def rightSide: RightSide

    // conversion to string to avoid recursion
    override def equals(obj: Any): Boolean = {
      def canEqual(obj: Any): Boolean = obj.isInstanceOf[Job]
      def equality(objRepr: String): Boolean = objRepr match {
        case _ if objRepr == AnyJob.toString => true
        case _ if this.toString == AnyJob.toString => true
        case _ => this.toString == objRepr
      }

      canEqual(obj) && equality(obj.toString)
    }
  }

  case object AnyJob extends Job {
    val leftSide: LeftSide = null
    val rightSide: RightSide = null
  }

  trait Casters extends Job {
    val leftSide: LeftSide = BodyCasters
    val rightSide: RightSide = AccessoriesInt
  }
  trait Healers extends Job {
    val leftSide: LeftSide = BodyHealers
    val rightSide: RightSide = AccessoriesMnd
  }
  trait Mnks extends Job {
    val leftSide: LeftSide = BodyMnks
    val rightSide: RightSide = AccessoriesStr
  }
  trait Tanks extends Job {
    val leftSide: LeftSide = BodyTanks
    val rightSide: RightSide = AccessoriesVit
  }
  trait Ranges extends Job {
    val leftSide: LeftSide = BodyRanges
    val rightSide: RightSide = AccessoriesDex
  }

  case object PLD extends Tanks
  case object WAR extends Tanks
  case object DRK extends Tanks
  case object GNB extends Tanks

  case object WHM extends Healers
  case object SCH extends Healers
  case object AST extends Healers

  case object MNK extends Mnks
  case object DRG extends Job {
    val leftSide: LeftSide = BodyDrgs
    val rightSide: RightSide = AccessoriesStr
  }
  case object NIN extends Job {
    val leftSide: LeftSide = BodyNins
    val rightSide: RightSide = AccessoriesDex
  }
  case object SAM extends Mnks

  case object BRD extends Ranges
  case object MCH extends Ranges
  case object DNC extends Ranges

  case object BLM extends Casters
  case object SMN extends Casters
  case object RDM extends Casters

  lazy val available: Seq[Job] =
    Seq(PLD, WAR, DRK, GNB, WHM, SCH, AST, MNK, DRG, NIN, SAM, BRD, MCH, DNC, BLM, SMN, RDM)
  lazy val availableWithAnyJob: Seq[Job] = available.prepended(AnyJob)

  def withName(job: String): Job.Job =
    availableWithAnyJob.find(_.toString.equalsIgnoreCase(job.toUpperCase)) match {
      case Some(value) => value
      case None if job.isEmpty => AnyJob
      case _ => throw new IllegalArgumentException("Invalid or unknown job")
    }
}
