package mycluster.cluster

import org.scalatest.{Matchers, WordSpec}

class ClusterConfigTest extends WordSpec with Matchers {
  "ClusterConfig.apply(str)" should {
    "create a config from a string" in {

      val conf = ClusterConfig(
        """
    my.cluster {
      port : 19
      host : foo
      seed-nodes = [
        "x",
        "y"]
    }
    """)

      conf.config.getString("akka.actor.provider") shouldBe "cluster"
      conf.config.getString("akka.remote.netty.tcp.hostname") shouldBe "foo"
      conf.config.getInt("akka.remote.netty.tcp.port") shouldBe 19
      import scala.collection.JavaConverters._
      conf.config.getStringList("akka.cluster.seed-nodes").asScala.toList should contain only("x", "y")
    }
  }

}
