package agido.pp.spray

import java.util.concurrent.TimeUnit

import scala.util.Success

import agido.pp.spray.estimates.Estimate
import agido.pp.spray.estimates.GetAllEstimatesQuery
import agido.pp.spray.estimates.GetAllEstimatesResponse
import agido.pp.spray.estimates.GetEstimatesQuery
import agido.pp.spray.estimates.GetEstimatesResponse
import agido.pp.spray.estimates.SetEstimate
import agido.pp.spray.estimates.TaskEstimatePersistentActor
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

/**
 * Parent actor of TaskEstimateActor
 *
 * Forwards all messages to the right TaskEstimateActor and sends a
 * reply to the http actor
 *
 * @author schallab
 *
 */
class EstimateActor extends Actor {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec = context.dispatcher

  override def preStart = {
    println(s"estimate actor $self started")
  }

  /**
   * get the child actor this taskId; create it if it does not exist yet
   */
  def actorForTaskEstimate(taskId: String): ActorRef = {
    val actorName = s"actor-$taskId"
    println(s"$self looking up actor $actorName (all ${context.children.size} children: ${context.children.toList})")

    context.child(actorName) match {
      case Some(actor) =>
        println(s"found existing $actor")
        actor
      case None =>
        println(s"creating new TaskEstimateActor for $taskId")
        val props = Props(classOf[TaskEstimatePersistentActor], taskId)
        context.actorOf(props, actorName);
    }
  }

  /**
   * Handle messages by dispatching them to the right task actor
   *
   * This is all very asynchronous
   *
   */
  def receive = {
    case taskId: String =>
      // need to store this in order to send the answer to the right actor
      // maybe the task actor should talk directly to the httpsender? not implemented yet
      val httpsender = sender()
      println(s"looking up estimates for task $taskId")
      val fResponse = actorForTaskEstimate(taskId) ? GetAllEstimatesQuery(taskId)
      fResponse onComplete {
        case Success(GetAllEstimatesResponse(estimates)) =>
          println(s"got estimates $estimates")
          httpsender ! estimates

        case x => // other response or failure
          throw new Exception(s"unexpected response $x")
      }

    case (taskId: String, estimate: Estimate) =>
      println(s"setting an estimate for task $taskId")
      actorForTaskEstimate(taskId) ! SetEstimate(taskId, estimate)

    case (taskId: String, user: String) =>
      // as above
      val httpsender = sender()
      println(s"looking up estimates for task $taskId & user $user")

      val fResponse = actorForTaskEstimate(taskId) ? GetEstimatesQuery(taskId, user)
      fResponse onComplete {
        case Success(GetEstimatesResponse(estimates)) =>
          // some light postprocessing to add "missing" estimate types and sorting
          val missingTypes = Estimate.defaultTypes filter
            (defaulttype => !estimates.exists(_.`type` == defaulttype))

          val combinedEstimates = estimates ::: (missingTypes map (Estimate(user, _, None)))
          println(s"got estimates for $taskId/$user: $combinedEstimates")

          httpsender ! Estimate.sorted(combinedEstimates)

        case x => // other response or failure
          throw new Exception(s"unexpected response $x")
      }
  }
}