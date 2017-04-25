package mycluster.cluster.client


import akka.actor.{ActorSystem, RootActorPath}
import mycluster.cluster.MyClusterConfig
import com.typesafe.config.{Config, ConfigFactory, ConfigUtil}

object ClientConfig {


  private def clusterClientConf: Config = ConfigFactory.parseResourcesAnySyntax("cluster-client").ensuring(!_.isEmpty)

  def defaultConfig: Config = ConfigFactory.load(clusterClientConf)

  def seed(cluster: String, host: String, port: Int) = {
    s"akka.tcp://${cluster}@${host}:$port"
  }

  def withSeeds(name: String, nodes: Set[String]) = {
    val seeds = nodes.map(ConfigUtil.quoteString).mkString("seed-nodes = [", ", ", "]")
    val seedConf =
      ConfigFactory.parseString(
        s"""my.cluster {
           |  name : $name
           |  $seeds
           |}""".stripMargin)
    ClientConfig(seedConf.withFallback(ClientConfig.defaultConfig))
  }
}

case class ClientConfig(config: Config = ClientConfig.defaultConfig) {

  val my = MyClusterConfig(config.getConfig("my.cluster"))

  def initialContacts = my.seedAddresses.ensuring(_.nonEmpty).map { addr =>
    RootActorPath(addr) / "system" / "receptionist"
  }

  def system = implicits.system

  object implicits {
    implicit val timeout = my.timeout
    lazy val system = ActorSystem(my.name, config)
    implicit lazy val ec = system.dispatcher
  }

}