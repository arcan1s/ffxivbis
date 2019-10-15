package me.arcanis.ffxivbis.models

case class PlayerIdWithCounters(partyId: String,
                                job: Job.Job,
                                nick: String,
                                isRequired: Boolean,
                                priority: Int,
                                bisCountTotal: Int,
                                lootCount: Int,
                                lootCountBiS: Int,
                                lootCountTotal: Int)
  extends PlayerIdBase {
  import PlayerIdWithCounters._

  def playerId: PlayerId = PlayerId(partyId, job, nick)

  def gt(that: PlayerIdWithCounters, orderBy: Seq[String]): Boolean =
    withCounters(orderBy) > that.withCounters(orderBy)

  private val counters: Map[String, Int] = Map(
    "isRequired" -> (if (isRequired) 1 else 0),
    "priority" -> priority,
    "bisCountTotal" -> bisCountTotal,
    "lootCount" -> lootCount,
    "lootCountBiS" -> lootCountBiS,
    "lootCountTotal" -> lootCountTotal) withDefaultValue 0

  private def withCounters(orderBy: Seq[String]): PlayerCountersComparator =
    PlayerCountersComparator(orderBy.map(counters): _*)
}

object PlayerIdWithCounters {
  private case class PlayerCountersComparator(values: Int*) {
    def >(that: PlayerCountersComparator): Boolean = {
      @scala.annotation.tailrec
      def compareLists(left: List[Int], right: List[Int]): Boolean =
        (left, right) match {
          case (hl :: tl, hr :: tr) => if (hl == hr) compareLists(tl, tr) else hl > hr
          case (_ :: _, Nil) => true
          case (_, _) => false
        }
      compareLists(values.toList, that.values.toList)
    }
  }
}
