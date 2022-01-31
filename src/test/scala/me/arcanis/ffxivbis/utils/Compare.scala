package me.arcanis.ffxivbis.utils

object Compare {

  def mapEquals[K, T](left: Map[K, T], right: Map[K, T]): Boolean =
    left.forall {
      case (key, value) => right.contains(key) && right(key) == value
    }

  def seqEquals[T](left: Seq[T], right: Seq[T]): Boolean =
    left.groupBy(identity).view.mapValues(_.size).forall {
      case (key, count) => right.count(_ == key) == count
    }
}
