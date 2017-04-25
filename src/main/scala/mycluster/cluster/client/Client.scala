package mycluster.cluster.client

import akka.cluster.client.ClusterClient.SendToAll
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import mycluster.cluster.{CheckMembers, CheckMembersResponse, ClusterStateActor}
import akka.pattern.ask

import scala.concurrent.{Await, Future}

case class Client(config: ClientConfig) {

  import config.implicits._

  val clusterClient = system.actorOf(
    ClusterClient.props(
      ClusterClientSettings(system)
        .withInitialContacts(config.initialContacts)),
    "clusterClient")

  def sendSync(msg: String) = Await.result(send(msg), timeout.duration)

  def membersSync(withRoles: String*) = Await.result(members(withRoles: _*), timeout.duration)

  def send(msg: String) = {
    val future = clusterClient ? SendToAll(s"/user/${ClusterStateActor.name}", msg)
    future.mapTo[String]
  }

  def members(withRoles: String*) = {
    val future = clusterClient ? SendToAll(s"/user/${ClusterStateActor.name}", CheckMembers(withRoles.toSet))
    future.mapTo[CheckMembersResponse].map(_.members)
  }

}
