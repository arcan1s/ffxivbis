package me.arcanis.ffxivbis.service.impl

import akka.actor.Props
import me.arcanis.ffxivbis.service.Database
import me.arcanis.ffxivbis.storage.DatabaseProfile

import scala.concurrent.ExecutionContext

class DatabaseImpl extends Database
  with DatabaseBiSHandler with DatabaseLootHandler
  with DatabasePartyHandler with DatabaseUserHandler {

  implicit val executionContext: ExecutionContext = context.dispatcher
  val profile = new DatabaseProfile(executionContext, context.system.settings.config)

  override def receive: Receive =
    bisHandler orElse lootHandler orElse partyHandler orElse userHandler
}

object DatabaseImpl {
  def props: Props = Props(new DatabaseImpl)
}
