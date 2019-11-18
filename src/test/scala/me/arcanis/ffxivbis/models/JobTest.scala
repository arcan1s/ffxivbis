package me.arcanis.ffxivbis.models

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class JobTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  "job model" must {

    "create job from string" in {
      Job.available.foreach { job =>
        Job.withName(job.toString) shouldEqual job
      }
    }

    "return AnyJob" in {
      Job.withName("anyjob") shouldEqual Job.AnyJob
    }

    "fail on unknown job" in {
      an [IllegalArgumentException] should be thrownBy Job.withName("random string")
    }

    "equal AnyJob to others" in {
      Job.available.foreach { job =>
        Job.AnyJob shouldBe job
      }
    }

  }
}
