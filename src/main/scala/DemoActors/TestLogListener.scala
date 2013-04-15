package DemoActors

import akka.actor.Actor
import akka.event.Logging._

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/13/13
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
class MyEventListener extends Actor {
  def receive = {
    case Error(cause, logSource, logClass, message) => println("got it")

  }
}


