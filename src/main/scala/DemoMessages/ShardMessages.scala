package DemoMessages

/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
abstract trait ShardMessage
case object ShardRequest extends ShardMessage
case object ShardStateRequest extends ShardMessage
case class FaultIt(waitBeforeCrash : Int) extends ShardMessage
case class RecycleIt(waitBeforeCrash : Int) extends ShardMessage
case class CrashIt(waitBeforeCrash : Int) extends ShardMessage
case class EndOfTheWorld(waitBeforeCrash : Int) extends ShardMessage
case class Ping(i : String) extends  ShardMessage



case object ProxyMessage



