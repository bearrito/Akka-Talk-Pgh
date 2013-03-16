package DemoMessages

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/24/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
import akka.actor._

case class RegisterWorker(worker : ActorRef, supervisor : ActorRef)
case object CrashDetected