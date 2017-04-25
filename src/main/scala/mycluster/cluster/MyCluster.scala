package mycluster.cluster

import akka.actor.{Props, Terminated}
import akka.cluster.Member

import scala.collection.immutable.SortedSet
import scala.concurrent.Future

case class MyCluster(conf: ClusterConfig) {

  import conf._
  import conf.implicits._

  def terminate: Future[Terminated] = system.terminate()
  def name = conf.my.name
  def port = conf.my.port
  def roles = conf.my.roles

  val clusterStateActor = system.actorOf(ClusterStateActor.props, ClusterStateActor.name)

  def members(roles: Set[String]): Future[SortedSet[Member]] = {
    import akka.pattern.ask
    (clusterStateActor ? CheckMembers(roles)).mapTo[CheckMembersResponse].map(_.members)
  }
}
