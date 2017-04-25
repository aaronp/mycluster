package mycluster.cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.client.ClusterClientReceptionist
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}

abstract class BaseActor extends Actor with ActorLogging {

  val cluster = Cluster(context.system)


  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }
  lazy val mediator = DistributedPubSub(context.system).mediator

  val receptionist = ClusterClientReceptionist(context.system)

  override def unhandled(message: Any): Unit = {
    super.unhandled(message)
    sys.error(s"${self.path} couldn't handle $message")
  }
}
