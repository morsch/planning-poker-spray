package agido.pp.spray.estimates

import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure


/**
 * Integrate the given estimate.
 * 
 * Used both as a command received from other actors as well as a persisted Event
 */
case class SetEstimate(taskId: String, estimate: Estimate)

/**
 * Message: Query for estimates
 */
case class GetEstimatesQuery(taskId: String, user: String)
/**
 * Message Response
 */
case class GetEstimatesResponse(estimates: List[Estimate])

/**
 * Manages the estimates of one user for one task
 * 
 * @author schallab
 */
class SingleEstimatePersistentActor(taskid: String, user: String)
  extends PersistentActor {

  override def persistenceId = s"estimates-for-$taskid-$user"

  override def preStart = {
    super.preStart()
    println(s"persistent actor $persistenceId starting")
  }

  override def postStop = {
    super.postStop()
    println(s"persistent actor $persistenceId stopped")
  }

  // this is the persistent actors mutable state
  val estimates = mutable.Map[String, Double]()

  // update own state
  def updateEstimates(e: SetEstimate) = {
    e.estimate.amount match {
      case Some(amount) =>
        estimates.put(e.estimate.`type`, amount)
      case None => estimates.remove(e.estimate.`type`)
    }
  }

  // recover from journal
  def receiveRecover: Receive = {
    case e: SetEstimate =>
      println(s"got recover event $e")
      updateEstimates(e)
  }

  // handle commands and queries
  def receiveCommand: Receive = {
    case e: SetEstimate =>
      println(s"got command $e")
      persist(e) { pe =>
        println(s"persisted event $pe")
        updateEstimates(pe)
      }

    case GetEstimatesQuery(_, _) =>
      sender() ! GetEstimatesResponse(makeEstimates(user, estimates.toMap))
  }

  // translate from internal map representation to List[Estimate] used everywhere else
  def makeEstimates(user: String, estimatesMap: Map[String, Double]): List[Estimate] = {
    val iterable = estimatesMap map {
      case (typ, amount) =>
        Estimate(user, typ, Some(amount))
    }
    iterable.toList
  }
}