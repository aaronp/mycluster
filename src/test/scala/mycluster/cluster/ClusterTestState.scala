package mycluster.cluster

import mycluster.cluster.client.{Client, ClientConfig}

import scala.concurrent.Future

case class ClusterTestState(
                             clusterByName: Map[String, MyCluster] = Map.empty,
                             clientByName: Map[String, Client] = Map.empty
                           ) {

  def namesByPort: Map[Int, String] = clusterByName.mapValues(_.port).map(_.swap)

  def sendMessage(client: String, msg: String) = {
    clientByName(client).sendSync(msg)
  }
  def connectClientToNode(client: String, node: String) = {
    require(!clientByName.contains(client))
    val conf = clusterByName(node).conf.my
    val seeds = ClientConfig.seed(conf.name, conf.host, conf.port)
    val cc = ClientConfig.withSeeds(client, Set(seeds))
    copy(clientByName = clientByName.updated(client, Client(cc)))
  }

  def nodeWithPortAndRoles(name: String, port: Int, roles: Set[String]) = {
    require(!clusterByName.contains(name))
    val c = MyCluster(ClusterConfig(port, roles = roles))
    copy(clusterByName = clusterByName.updated(name, c))
  }

  def nodeWithConfig(name: String, config: String) = {
    require(!clusterByName.contains(name))
    val c = MyCluster(ClusterConfig(config))
    copy(clusterByName = clusterByName.updated(name, c))
  }

  def reset(): Future[ClusterTestState] = {
    if (clusterByName.isEmpty) {
      Future.successful(this)
    } else {
      val firstConf = clusterByName.values.head.conf
      import firstConf.implicits._
      val doneFutures = clusterByName.values.map(_.terminate)
      Future.sequence(doneFutures).map(_ => ClusterTestState())
    }
  }

}
