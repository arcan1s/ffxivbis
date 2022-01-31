/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

sealed trait Job extends Equals {

  def leftSide: Job.LeftSide

  def rightSide: Job.RightSide

  // conversion to string to avoid recursion
  override def canEqual(that: Any): Boolean = that.isInstanceOf[Job]

  override def equals(obj: Any): Boolean = {
    def equality(objRepr: String): Boolean = objRepr match {
      case _ if objRepr == Job.AnyJob.toString => true
      case _ if this.toString == Job.AnyJob.toString => true
      case _ => this.toString == objRepr
    }

    canEqual(obj) && equality(obj.toString)
  }
}

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

  case object AnyJob extends Job {
    override val leftSide: LeftSide = null
    override val rightSide: RightSide = null
  }

  trait Casters extends Job {
    override val leftSide: LeftSide = BodyCasters
    override val rightSide: RightSide = AccessoriesInt
  }
  trait Healers extends Job {
    override val leftSide: LeftSide = BodyHealers
    override val rightSide: RightSide = AccessoriesMnd
  }
  trait Mnks extends Job {
    override val leftSide: LeftSide = BodyMnks
    override val rightSide: RightSide = AccessoriesStr
  }
  trait Drgs extends Job {
    override val leftSide: LeftSide = BodyDrgs
    override val rightSide: RightSide = AccessoriesStr
  }
  trait Nins extends Job {
    override val leftSide: LeftSide = BodyNins
    override val rightSide: RightSide = AccessoriesDex
  }
  trait Tanks extends Job {
    override val leftSide: LeftSide = BodyTanks
    override val rightSide: RightSide = AccessoriesVit
  }
  trait Ranges extends Job {
    override val leftSide: LeftSide = BodyRanges
    override val rightSide: RightSide = AccessoriesDex
  }

  case object PLD extends Tanks
  case object WAR extends Tanks
  case object DRK extends Tanks
  case object GNB extends Tanks

  case object WHM extends Healers
  case object SCH extends Healers
  case object AST extends Healers
  case object SGE extends Healers

  case object MNK extends Mnks
  case object DRG extends Drgs
  case object RPR extends Drgs
  case object NIN extends Nins
  case object SAM extends Mnks

  case object BRD extends Ranges
  case object MCH extends Ranges
  case object DNC extends Ranges

  case object BLM extends Casters
  case object SMN extends Casters
  case object RDM extends Casters

  val available: Seq[Job] =
    Seq(PLD, WAR, DRK, GNB, WHM, SCH, AST, SGE, MNK, DRG, RPR, NIN, SAM, BRD, MCH, DNC, BLM, SMN, RDM)
  val availableWithAnyJob: Seq[Job] = available.prepended(AnyJob)

  def withName(job: String): Job =
    availableWithAnyJob.find(_.toString.equalsIgnoreCase(job)) match {
      case Some(value) => value
      case None if job.isEmpty => AnyJob
      case _ => throw new IllegalArgumentException(s"Invalid or unknown job $job")
    }
}
