package mycluster.cluster.client

import org.scalatest.{Matchers, WordSpec}

class ClientConfigTest extends WordSpec with Matchers {

  "ClientConfig.defaultConfig" should {
    "use a remote provider" in {
      val c = ClientConfig()
      c.config.getString("akka.actor.provider") shouldBe "remote"
      c.config.getString("my.cluster.name") shouldBe "my-client"
      c.config.getStringList("akka.extensions") shouldBe empty
    }
  }
}
