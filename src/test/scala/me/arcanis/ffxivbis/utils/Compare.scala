package me.arcanis.ffxivbis.utils

object Compare {
  def seqEquals[T](left: Seq[T], right: Seq[T]): Boolean =
    left.groupBy(identity).view.mapValues(_.size).forall {
      case (key, count) => right.count(_ == key) == count
    }
}
