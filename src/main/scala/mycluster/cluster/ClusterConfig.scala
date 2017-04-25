package mycluster.cluster

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Address, AddressFromURIString, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigUtil}

import concurrent.duration._

object ClusterConfig {

  def defaultConfig: Config = ConfigFactory.load()

  private def myConf: Config = ConfigFactory.parseResourcesAnySyntax("my-cluster").ensuring(!_.isEmpty)

  def defaultHost = "127.0.0.1"

  def apply(port: Int,
            seedNodes: Seq[(String, Int)] = Nil,
            roles: Set[String] = Set.empty,
            clusterName: String = "my-cluster",
            host: String = defaultHost): ClusterConfig = {

    val nodes = seedNodes.map {
      case (h, p) => val raw = s"akka.tcp://$clusterName@$h:$p"
        ConfigUtil.quoteString(raw)
    }

    apply {
      s"""my.cluster {
         |  host : ${host}
         |  port : ${port}
         |  name : ${clusterName}
         |  roles : ${roles.map(ConfigUtil.quoteString).mkString("[", ", ", "]")}
         |  ${if (nodes.nonEmpty) nodes.mkString("seed-nodes = [", ", ", "]") else ""}
         |}
     """.stripMargin
    }
  }

  def apply(configString: String): ClusterConfig = {
    val c = ConfigFactory.parseString(configString).withFallback(myConf)
    val conf = ConfigFactory.load(c)
    new ClusterConfig(conf)
  }
}

case class ClusterConfig(config: Config) {

  val my = MyClusterConfig(config.getConfig("my.cluster"))

  lazy val system = ActorSystem(my.name, config)

  object implicits {
    implicit val timeout = my.timeout
    implicit lazy val ec = system.dispatcher
  }

}

case class MyClusterConfig(config: Config) {
  val timeout: Timeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS).millis)
  val host = config.getString("host")
  val name = config.getString("name")
  val port = config.getInt("port")

  import scala.collection.JavaConverters._

  val seedNodes = config.getStringList("seed-nodes").asScala.toSet.ensuring(_.nonEmpty, "no seed-nodes set in configuration")

  val seedAddresses: Set[Address] = seedNodes.collect {
    case AddressFromURIString(addr) => addr
  }

  val roles = {
    val strings: Set[String] = config.getStringList("roles").asScala.toSet
    strings.map(_.trim)
  }

}
