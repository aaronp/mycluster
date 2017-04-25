package mycluster.cluster

import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging, Props, RootActorPath}

import scala.collection.immutable.SortedSet

object ClusterStateActor {
  def props = Props[ClusterStateActor]
  val name = "state"
}

class ClusterStateActor extends BaseActor {

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
    receptionist.registerService(self)
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  var members = SortedSet[Member]()

  def membersWithRoles(withRoles: Set[String]) = {
    if (withRoles.isEmpty) {
      members
    } else {
      members.filter(m => withRoles.exists(m.hasRole))
    }
  }

  def receive = {
    case SendTo(role, msg) =>
      membersWithRoles(Set(role)).foreach { m =>
        context.actorSelection(RootActorPath(m.address) / "user" / role) ! msg
      }
    case str : String => sender ! s"got: $str"
    case CheckMembers(withRoles) => sender ! CheckMembersResponse(membersWithRoles(withRoles))
    case state: CurrentClusterState =>
      log.info(s"${state.members.size} Current members: ${state.members.mkString(", ")}")
      members = state.members
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      members = members + member
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      members = members - member
    case MemberRemoved(member, previousStatus) =>
      log.info(
        "Member is Removed: {} after {}",
        member.address, previousStatus)
      members = members - member
    case _: MemberEvent => // ignore
  }


}
