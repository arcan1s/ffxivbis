package me.arcanis.ffxivbis.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import me.arcanis.ffxivbis.models.{BiS, Job, Piece}
import me.arcanis.ffxivbis.service.Ariyala

import scala.concurrent.{ExecutionContext, Future}

class AriyalaHelper(ariyala: ActorRef) {

  def downloadBiS(link: String, job: Job.Job)
                 (implicit executionContext: ExecutionContext, timeout: Timeout): Future[BiS] =
    (ariyala ? Ariyala.GetBiS(link, job)).mapTo[Seq[Piece]].map(BiS(_))
}
