package me.arcanis.ffxivbis.messages

import akka.actor.typed.Behavior

trait Message

object Message {
  type Handler = PartialFunction[Message, Behavior[Message]]
}
