package me.arcanis.ffxivbis.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class JobTest extends AnyWordSpecLike with Matchers {

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
        Job.AnyJob shouldEqual job
      }
    }

  }
}
