package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 7:52 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor.Actor
import akka.actor.ActorRef
import akka.transactor._
import scala.concurrent.stm._

class ShardActor extends  Actor{
  import DemoMessages._

  var requestsReceived : Int = 0
  val data = Ref(0)

  override def preStart() {
    println("Starting ShardActor instance hashcode # {}", this.hashCode())
  }
  override def postStop() {
    println("Stopping ShardActor instance hashcode # {}", this.hashCode())
    println("state :" + requestsReceived.toString)
  }

  def receive = {
    case ShardRequest => {
      println("got it")
      requestsReceived+=1
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
      sender ! requestsReceived
    }
    case Ping(m) => {
      val path  = context.self.path.toString()
      println(path)
      sender ! Ping(path)
    }

    case coordinated @ Coordinated(PostData(i,friend)) ⇒ {
      friend foreach (_ ! coordinated(PostData(i)))
      coordinated atomic { implicit t ⇒
        data transform (d => i)
      }
    }
    case GetData => {
      sender ! data.single.get
      //sender ! 2
      println("ss")
    }


  }
}
