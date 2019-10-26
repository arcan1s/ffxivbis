package me.arcanis.ffxivbis.models

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class JobTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "job model" must {

    "create job from string" in {
      Job.jobs.foreach { job =>
        Job.withName(job.toString) shouldEqual job
      }
    }

    "return AnyJob on unknown job" in {
      Job.withName("random string") shouldEqual Job.AnyJob
    }

    "equal AnyJob to others" in {
      Job.jobs.foreach { job =>
        Job.AnyJob shouldBe job
      }
    }

  }
}
