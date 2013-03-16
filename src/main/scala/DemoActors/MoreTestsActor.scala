package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/12/13
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor._
import DemoMessages.{ProxyMessage, CrashIt, FaultIt}
import akka.event.Logging

class MoreTestsActor extends Actor{

  var otherActor: Option[ActorRef] = None
  val log = Logging(context.system, this)

  def receive = {
     case FaultIt => {
      log.error("fault")
    }
     case CrashIt => {
       val e = new IllegalArgumentException()
       log.error(e,"crash")
       throw e
     }

     case d:ActorRef =>  {

       log.info("proxy ref received")
       otherActor = Some(d)
     }

     case ProxyMessage => {

       otherActor match {
         case Some(a) => a ! ProxyMessage
         case None => println("no proxy recipient")

       }


     }

  }

}
