package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 7:52 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor.Actor
import akka.pattern.ask
import scala.concurrent.duration._

class ShardActor extends  Actor{
  import DemoMessages._

  var state : Int = 0

  override def preStart() {
    println("Starting ShardActor instance hashcode # {}", this.hashCode())
  }
  override def postStop() {
    println("Stopping ShardActor instance hashcode # {}", this.hashCode())
    println("state :" + state.toString)
  }

  def receive = {
    case ShardRequest => {
      println("got it")
      state+=1
    }
    case FaultIt(i) =>
    {
      println("faulting")
      throw new NoSuchElementException()
    }

    case RecycleIt(i) => {
      println("recycling")
      throw new NullPointerException()

    }
    case CrashIt => {

      println("crashing")
      throw new IllegalArgumentException()
    }

    case ShardStateRequest =>{

      println("sending state request")
      sender ! state
    }

    case Ping(m) => {
      val path  = context.self.path.toString()
      println(path)
      sender ! Ping(path)
    }
  }

}
