package mycluster.cluster

import akka.cluster.Member

import scala.collection.immutable.SortedSet

sealed trait MyClusterRequest extends Serializable
case class CheckMembers(withRoles : Set[String]) extends MyClusterRequest
case class SendTo(role : String, msg : Any) extends MyClusterRequest

sealed trait MyClusterResponse extends Serializable
case class CheckMembersResponse(members : SortedSet[Member]) extends MyClusterResponse
