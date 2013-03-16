package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor._
import DemoMessages.ShardStateRequest
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import DemoMessages._
import concurrent.Await

class ShardFacade extends Actor{


  import akka.pattern.ask
  var singleShard = context.actorOf(Props[ShardActor],name = "shard")
  val monitor = context.actorOf(Props[ShardMonitor])
  implicit val timeout = akka.util.Timeout(5 seconds)

  override def preStart(){
     monitor ! RegisterWorker(singleShard,self)

  }
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 3 seconds) {

    case _: NoSuchElementException => Resume
    case _: NullPointerException => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }
  def receive = {
    case ShardRequest  => {
      singleShard !  ShardRequest
    }
    case FaultIt(i) => {
      singleShard ! FaultIt(i)
    }
    case RecycleIt(i) =>{
      singleShard !  RecycleIt(i)
    }
    case  CrashIt =>  {
      singleShard ! CrashIt
    }
    case ShardStateRequest => {
      singleShard.tell(ShardStateRequest,sender)
    }
    case CrashDetected => {
      singleShard = context.actorOf(Props[ShardActor])
    }
    case r : ActorRef => singleShard = r
  }



}
