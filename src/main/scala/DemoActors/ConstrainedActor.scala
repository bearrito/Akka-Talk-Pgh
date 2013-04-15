package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/12/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor._
class ConstrainedActor extends Actor{

  var state : Int = 0
  context.become(accepting)
  def receive = {
    case _ =>

  }

  def accepting : Receive = {

    case i => {
      state += 1
      println("msg" + i)
      println("state" + state.toString)

      sender ! "done"
      if (state == 2) {
        println("becoming busy")
        context.become(overloaded)

      }




    }

  }

  def overloaded : Receive= {
    case i =>  {
      println("I'm busy. give up")
      sender ! "busy"
    }
  }
}
