/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/17/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor._
class EventListener extends Actor {

  def receive = {

    case _ => println("foo")


  }
}
