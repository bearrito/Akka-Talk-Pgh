package DemoActors

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/17/13
 * Time: 8:15 PM
 * To change this template use File | Settings | File Templates.
 */
import akka.actor._
import akka.transactor.Coordinated
import DemoMessages.PostData
import concurrent.stm.Ref

class FaultyShardActor extends Actor{

  val data = Ref(0)
  def receive = {
    case coordinated @ Coordinated(PostData(i,friend)) ⇒ {
      friend foreach (_ ! coordinated(PostData(i)))
      coordinated atomic { implicit t ⇒
        data transform (d => d / 0)
      }
    }


  }

}
