package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/24/13
 * Time: 7:29 PM
 * To change this template use File | Settings | File Templates.
 */



import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Terminated

import DemoMessages._

import scala.collection.mutable.HashMap
class ShardMonitor extends Actor{

  var monitoredActors = new HashMap[ActorRef, ActorRef]

  def receive: Receive = {
    case t: Terminated =>
      if (monitoredActors.contains(t.actor)) {
        println("Received Worker Actor Termination Message -> "
          + t.actor.path)
        println("Sending message to Supervisor")
        val value: Option[ActorRef] = monitoredActors.get(t.actor)
        value.get ! CrashDetected
      }

    case msg: RegisterWorker =>
      context.watch(msg.worker)
      monitoredActors += msg.worker -> msg.supervisor
  }


}
