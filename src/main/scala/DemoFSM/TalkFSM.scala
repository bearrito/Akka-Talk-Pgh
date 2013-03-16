package DemoFSM



import akka.actor._
import scala.concurrent.duration._
/**
 * Created with IntelliJ IDEA.
 * User: me
 * Date: 3/14/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
abstract trait VendingState
abstract trait VendingData
abstract trait VendingSignal

case object Idle    extends VendingState
case object Vending extends  VendingState
case object Dispensing extends VendingState
case object EatsMoney extends VendingState

case class Uninitialized(change : Int, sodaCount : Int) extends VendingData
case class Data(money : Int,change : Int,sodaCount : Int) extends VendingData

case object Quarter extends VendingSignal
case object Dollar  extends VendingSignal
case object ReturnMoney extends  VendingSignal



class SodaFSM extends Actor with FSM[VendingState,VendingData] {

  startWith(Idle,Uninitialized(1000, 50))

  when(Idle){
    case Event(Quarter,u :Uninitialized) => goto(Vending) using Data(25,u.change,u.sodaCount)
    case Event(Dollar, u: Uninitialized) => goto(Dispensing) using Uninitialized(u.change + 100, u.sodaCount)
  }
  when(Vending){
  case Event(ReturnMoney,d : Data) => goto(Idle) using Uninitialized(d.change,d.sodaCount)
  case Event(Quarter,d : Data) => {
     if (d.money == 75) {
       goto(Dispensing) using(Uninitialized(d.change + 100,d.sodaCount))
     }
     else{
       goto(Vending) using(Data(d.money + 25,d.change,d.sodaCount))
     }

  }
  case Event(Dollar, d : Data) => {goto(Dispensing) using Uninitialized(d.change + 100,d.sodaCount)}
  case Event(ReturnMoney, d : Data)  => {goto(Idle) using Uninitialized(d.change, d.sodaCount)}
  }
  when(Dispensing, stateTimeout = 250 millis){
    case Event(StateTimeout,u : Uninitialized) => goto(Idle) using Uninitialized(u.change,u.sodaCount - 1)
  }
  when(EatsMoney)
  {
    case Event(Dollar,u:Uninitialized) => goto(EatsMoney)  using u.copy(u.change + 100,u.sodaCount)
    case Event(Quarter,u:Uninitialized) => goto(EatsMoney)  using u.copy(u.change + 25,u.sodaCount)

  }



}
