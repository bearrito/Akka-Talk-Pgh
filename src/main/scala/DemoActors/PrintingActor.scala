package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor.Actor
import DemoMessages.{SynchronousSimpleMessage, SimpleMessage}

class PrintingActor extends Actor {
  def receive = {
    case SimpleMessage(msg) => println(msg)
    case SynchronousSimpleMessage(msg) => sender ! SynchronousSimpleMessage("deadlocks be around here")
    case _ => println("I don't know what to do")
  }
}


