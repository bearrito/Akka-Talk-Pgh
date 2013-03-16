/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 2/23/13
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.routing.{ScatterGatherFirstCompletedRouter, BroadcastRouter, RoundRobinRouter}
import akka.testkit._
import akka.testkit.TestFSMRef._
import akka.actor._
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import concurrent.Await
import DemoFSM.SodaFSM
import java.lang.IllegalArgumentException
import org.scalatest._
import scala.concurrent.duration._


import DemoActors._
import DemoMessages._
import DemoFSM._


class DemoSpecs extends FunSuite {
  test("Verify we know how to create actor systems"){

    val systemName =  "demoSystem"
    val system = ActorSystem(systemName)
    assert(system.name == systemName)
    system.shutdown()
  }
  test("Verify we know how to send an actor a message asynchronously"){

    val systemName =     "demoSystem"
    val system = ActorSystem(systemName)
    val printer = system.actorOf(Props[PrintingActor],name = "printer")
    val msg = SimpleMessage("Hi guys!")
    printer ! msg
    system.shutdown()


  }
  test("Verify we know how to send an actor a message synchronously, but should not generally do it"){

    //What do you guys think this will do?
    implicit val timeout = akka.util.Timeout(1 seconds)

    val systemName =     "demoSystems"
    val system = ActorSystem(systemName)
    val printer = system.actorOf(Props[PrintingActor],name = "printer")
    val msg = SynchronousSimpleMessage("Hi guys!")
    val response = printer ? msg
    val result = Await.result(response,timeout.duration)
    assert(true == result.isInstanceOf[SynchronousSimpleMessage])
    system.shutdown()


  }
  test("Verify we know how to reference actors"){


    val systemName =     "demoSystems"
    val system = ActorSystem(systemName)
    val printer = system.actorOf(Props[PrintingActor],name = "printer")
    val msg = SimpleMessage("Hi guys!")

    // Check the type
    printer ! msg
    val expectedPath = "akka://demoSystems/user/printer"
    val maybePrinter = system.actorFor(expectedPath)

    assert(maybePrinter.path.toString == expectedPath)
    assert(maybePrinter == printer)

    maybePrinter !  SimpleMessage("Hello again!")
    system.shutdown()
  }
  test("Verify we know how to reference actors in a hierarchy"){

    val systemName =     "demoSystems"
    val system = ActorSystem(systemName)
    val facade = system.actorOf(Props[ShardFacade],name = "facade")
    val backdoorShardPath = "akka://demoSystems/user/facade/shard"
    val request = ShardRequest
    val backdoorShard = system.actorFor(backdoorShardPath)

    assert(backdoorShard.path.toString == backdoorShardPath)
    backdoorShard ! request

  }
  test("Verify we know how to properly test"){

    import akka.testkit.TestActorRef

      val systemName =     "demoSystems"
      implicit val system = ActorSystem(systemName)

      val testActorRef = TestActorRef[ShardActor]
      val actorRef =   testActorRef.actorRef
      assert(testActorRef.underlyingActor.state == 0)
      actorRef ! ShardRequest
      assert(testActorRef.underlyingActor.state == 1)
      system.shutdown()




  }
  test("Verify we know how to properly test again"){

    import akka.testkit.TestActorRef

    val systemName =     "demoSystems"
    implicit val system = ActorSystem(systemName)

    val actorRef = TestActorRef[ShardActor]

    assert(actorRef.underlyingActor.state == 0)
    actorRef ! ShardRequest
    assert(actorRef.underlyingActor.state == 1)

    system.shutdown()




  }
  test("Verify we know how to test in the middle"){
    val systemName =     "demoSystems"
    implicit val system = ActorSystem(systemName)
    val actorRef = TestActorRef[ShardActor]
    intercept[NullPointerException] { actorRef.receive(RecycleIt(2)) }
  }
}
class DemoSpecs2 (_system : ActorSystem)  extends TestKit(_system) with FunSuite with ImplicitSender  with BeforeAndAfterAll{

  def this() = this(ActorSystem("demo"))


  test("Verify shard state request returns expected"){
    val actor = system.actorOf(Props[ShardActor])
    actor ! ShardStateRequest
    expectMsg(0)

    actor ! ShardRequest

    actor ! ShardStateRequest
    expectMsg(1)


  }
  test("Verify we can hotswap"){
    val actor = system.actorOf(Props[ConstrainedActor])
    actor ! "do it"
    expectMsg("done")
    actor ! "do it"
    expectMsg("done")
    actor ! "do it now!"
    expectMsg("busy")
  }
  test("Verify router in roundrobin") {

    implicit val timeout = akka.util.Timeout(10 seconds)

    val roundRobin = system.actorOf(Props[ShardActor].withRouter(
      RoundRobinRouter(nrOfInstances = 2)))
    println("Name *************************" + system.name)
    roundRobin ! Ping("ping")
    roundRobin ! Ping("ping")
    receiveN(2,timeout.duration)

  }
  test("Verify router in roundrobin more correctly") {

    implicit val timeout = akka.util.Timeout(10 seconds)
    val roundRobin = system.actorOf(Props[ShardActor].withRouter(
      RoundRobinRouter(nrOfInstances = 2)),name = "router")
    println("Name *************************" + system.name)
    roundRobin ! Ping("ping")
    roundRobin ! Ping("ping")

    expectMsgAllOf(timeout.duration,Ping("akka://demo/user/router/$b"),Ping("akka://demo/user/router/$a"))
  }
  test("Verify router broadcasts"){
    implicit val timeout = akka.util.Timeout(10 seconds)
    val broadcast = system.actorOf(Props[ShardActor].withRouter(
      BroadcastRouter(nrOfInstances = 5)))

      broadcast ! Ping("ping")

      receiveN(5,timeout.duration)


  }
  test("Verify router scatters and gathers"){
    implicit val timeout = akka.util.Timeout(10 seconds)
    val farmer = system.actorOf(Props[ShardActor].withRouter(
      ScatterGatherFirstCompletedRouter(nrOfInstances = 5,within = 2 seconds)))
    val crop = farmer ? Ping("plant")
    val result = Await.result(crop, timeout.duration)
    assert(result.isInstanceOf[Ping])


  }
  override def afterAll {
    system.shutdown()
  }


}
class DemoSpecs3 extends FunSuite{
  test("Verify log intercept"){

    val systemName =     "demoSystems"
    implicit val mysys = ActorSystem(systemName,ConfigFactory.parseString("""akka.event-handlers = ["akka.testkit.TestEventListener"]
                                                                          """))
    val actor = mysys.actorOf(Props[MoreTestsActor],name = "tests")
    EventFilter.error(occurrences = 1) intercept {
      actor ! FaultIt

    }

    mysys.shutdown()

  }
  test("Verify more specific log intercept"){

    val systemName =     "demoSystems"
    implicit val mysys = ActorSystem(systemName,ConfigFactory.parseString("""akka.event-handlers = ["akka.testkit.TestEventListener"]
                                                                          """))
    val actor = mysys.actorOf(Props[MoreTestsActor],name = "tests")
    EventFilter.error(message = "fault",occurrences = 1) intercept {
      actor ! FaultIt

    }

    mysys.shutdown()

  }
  test("Verify very specific log intercept"){

    val systemName =     "demoSystems"
    implicit val mysys = ActorSystem(systemName,ConfigFactory.parseString("""akka.event-handlers = ["akka.testkit.TestEventListener"]
                                                                          """))
    val actor = mysys.actorOf(Props[MoreTestsActor],name = "tests")
    EventFilter[IllegalArgumentException](message = "crash",occurrences = 1) intercept {
      actor ! CrashIt

    }

    mysys.shutdown()

  }
  test("Verify probe receives message"){

    val systemName =     "demoSystems"
    implicit val mysys = ActorSystem(systemName,ConfigFactory.parseString("""akka.event-handlers = ["akka.testkit.TestEventListener"] """))
    val actor = mysys.actorOf(Props[MoreTestsActor],name = "tests")
    val probe = TestProbe()
    EventFilter.info(message = "proxy ref received",occurrences = 1) intercept {
      actor ! probe.testActor

    }

    actor ! ProxyMessage
    probe.expectMsg(ProxyMessage)
    val newProbe = TestProbe()
    actor ! newProbe.testActor
    actor ! ProxyMessage
    newProbe.expectMsg(ProxyMessage)
    probe.expectNoMsg()




  }
}
class DemoSpecs4 (_system : ActorSystem)  extends TestKit(_system) with FunSuite with ImplicitSender  with BeforeAndAfterAll{

  def this() = this(ActorSystem("demo"))
  test("Verify we know how to crash and restart with old state"){

    implicit val timeout = akka.util.Timeout(10 seconds)
    val facade = system.actorOf(Props[ShardFacade])
    val req = ShardRequest
    facade ! req
    facade ! req
    facade ! FaultIt(2)
    facade ! ShardStateRequest
    expectMsg(timeout.duration,2)

  }
  test("Verify we know how to crash and restart with new state "){

    implicit val timeout = akka.util.Timeout(10 seconds)
    val facade = system.actorOf(Props[ShardFacade])

    expectNoMsg()
    val req = ShardRequest
    facade ! req
    facade ! req
    facade ! req
    facade ! RecycleIt
    facade ! ShardStateRequest

    expectMsg(timeout.duration,0)
  }
  test("Verify we know how to not crash"){

    implicit val timeout = akka.util.Timeout(10 seconds)
    val facade = system.actorOf(Props[ShardFacade])

    val req = ShardRequest
    facade ! req
    facade ! req
    facade ! req
    facade ! req
    facade ! req
    facade ! ShardStateRequest
    expectMsg(timeout.duration,5)

    facade ! CrashIt


    within(1000 millis){expectNoMsg(timeout.duration)}
    facade ! ShardRequest
    facade ! ShardRequest
    facade ! ShardRequest
    expectMsg(timeout.duration,0)


  }

  test("Verify FSM gives us a soda"){
    implicit val timeout = akka.util.Timeout(2 seconds)
    val fsm = TestFSMRef(new SodaFSM)
    fsm.setState(stateName = Idle,stateData = Uninitialized(1000,5))
    fsm ! Dollar
    awaitCond(fsm.stateName == Idle, max = timeout.duration)
    assert(fsm.stateData == Uninitialized(1100,4))
  }
  test("Verify FSM keeps track of money"){

      val fsm = TestFSMRef(new SodaFSM)
      fsm.setState(stateName = Vending,stateData = Data(50,100,5))
      fsm ! Quarter
      within(500 millis){
        assert(fsm.stateName == Vending)
        assert(fsm.stateData == Data(75,100,5))

      }


  }
  test("Verify FSM gives back money"){

    val fsm = TestFSMRef(new SodaFSM)
    fsm.setState(stateName = Vending,stateData = Data(50,100,5))
    fsm ! ReturnMoney
    within(500 millis){
      assert(fsm.stateName == Idle)
      assert(fsm.stateData == Uninitialized(100,5))

    }


  }


  override def afterAll {
    system.shutdown()
  }


}
