/*
 * Copyright (c) 2019-2022 Evgeniy Alekseev.
 *
 * This file is part of ffxivbis
 * (see https://github.com/arcan1s/ffxivbis).
 *
 * License: 3-clause BSD, see https://opensource.org/licenses/BSD-3-Clause
 */
package me.arcanis.ffxivbis.models

case class PlayerIdWithCounters(
  partyId: String,
  job: Job,
  nick: String,
  isRequired: Boolean,
  priority: Int,
  bisCountTotal: Int,
  lootCount: Int,
  lootCountBiS: Int,
  lootCountTotal: Int
) extends PlayerIdBase {
  import PlayerIdWithCounters._

  def gt(that: PlayerIdWithCounters, orderBy: Seq[String]): Boolean =
    withCounters(orderBy) > that.withCounters(orderBy)

  def playerId: PlayerId = PlayerId(partyId, job, nick)

  private val counters: Map[String, Int] = Map(
    "isRequired" -> (if (isRequired) 1 else 0), // true has more priority
    "priority" -> -priority, // the less value the more priority
    "bisCountTotal" -> bisCountTotal, // the more pieces in bis the more priority
    "lootCount" -> -lootCount, // the less loot got the more priority
    "lootCountBiS" -> -lootCountBiS, // the less bis pieces looted the more priority
    "lootCountTotal" -> -lootCountTotal
  ).withDefaultValue(0) // the less pieces looted the more priority

  private def withCounters(orderBy: Seq[String]): PlayerCountersComparator =
    PlayerCountersComparator(orderBy.map(counters): _*)
}

object PlayerIdWithCounters {

  private case class PlayerCountersComparator(values: Int*) {

    def >(that: PlayerCountersComparator): Boolean = {
      @scala.annotation.tailrec
      def compare(left: Seq[Int], right: Seq[Int]): Boolean =
        (left, right) match {
          case (hl :: tl, hr :: tr) => if (hl == hr) compare(tl, tr) else hl > hr
          case (_ :: _, Nil) => true
          case (_, _) => false
        }
      compare(values, that.values)
    }
  }
}
